package edu.sb.cookbook.service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import edu.sb.cookbook.persistence.Document;
import edu.sb.cookbook.persistence.IngredientType;
import edu.sb.cookbook.persistence.Person;
import edu.sb.cookbook.persistence.Recipe;

@Path("persons")
public class PersonService {

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Returns the people matching the given filter criteria, with missing
	 * parameters identifying omitted criteria. Search criteria should be query
	 * parameters containing any “normal” property of both people and their
	 * composites, except identity and passwordHash, plus int values for
	 * result-offset and result-limit which define a paging range. The JPA query
	 * should return the matching Person-IDs, which are used subsequenty to query
	 * the people from the 2nd level cache using "entityManager.find()", and then
	 * sorted by name and email → best use Collection-Streams for the latter two
	 * steps.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response queryPeople(@QueryParam("result-offset") @PositiveOrZero Integer resultOffset,
			@QueryParam("result-limit") @PositiveOrZero Integer resultLimit) {
		try {
			TypedQuery<Long> query = entityManager.createQuery("SELECT p.id FROM Person p", Long.class);
			if (resultOffset != null)
				query.setFirstResult(resultOffset);
			if (resultLimit != null)
				query.setMaxResults(resultLimit);

			List<Long> personIds = query.getResultList();
			List<Person> people = personIds.stream().map(id -> entityManager.find(Person.class, id))
					.sorted(Comparator.comparing(Person::getName).thenComparing(Person::getEmail))
					.collect(Collectors.toList());

			return Response.ok(people).build();
		} catch (Exception e) {
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

	/**
	 * Otherwise it updates the corresponding person with the given template data,
	 * which only adminstrators or the person itself may perform. Make sure
	 * non-administrators cannot upgrade their group. Optionally, a new password may
	 * be set using the header field “X-Set-Password”. Returns the affected person's
	 * identity as text/plain. Only Admintrators or the given person may perform
	 * this operation.
	 */

	// HTTP Signature: POST people (in: JSON, out: text/plain)
	// HTTP Signature: POST people (in: JSON, out: text/plain)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response createOrUpdatePerson(
			// Inserts or updates a person from template data within the HTTP request body.
			@HeaderParam("X-Requester-Identity") @Positive long requesterIdentity, Person person) {
		try {
			// Make sure non-administrators cannot upgrade their group.
			if (!isAdministrator(requesterIdentity) && requesterIdentity != person.getIdentity()) {
				return Response.status(Response.Status.FORBIDDEN)
						.entity("Only administrators or the person itself may perform this operation.").build();
			}

			// It creates a new person if the given template's identity is zero, which
			// solely admins may perform.
			// Otherwise it updates the corresponding person with the given template data
			if (person.getIdentity() == 0) {
				// Ensure that only administrators can create a new person with identity zero
				if (!isAdministrator(requesterIdentity)) {
					return Response.status(Response.Status.FORBIDDEN)
							.entity("Only administrators may create a new person with identity zero.").build();
				}
				// The default avatar with ID 1 shall be associated during creation if none is
				// provided.
				Document defaultAvatar = entityManager.find(Document.class, 1);
				person.setAvatar(defaultAvatar);

				entityManager.persist(person);
			} else {
				// If the person's identity is not zero, update the existing person with the
				// given template data
				Person existingPerson = entityManager.find(Person.class, person.getIdentity());
				if (existingPerson == null) {
					return Response.status(Response.Status.NOT_FOUND)
							.entity("Person not found for the provided identity.").build();
				}
				// Ensure that only administrators or the person itself can update the person's
				// data
				if (!isAdministrator(requesterIdentity) && requesterIdentity != existingPerson.getIdentity()) {
					return Response.status(Response.Status.FORBIDDEN)
							.entity("Only administrators or the person itself may update this person's data.").build();
				}
				// Set the avatar if none is provided
				if (person.getAvatar() == null) {
					Document defaultAvatar = entityManager.find(Document.class, 1);
					person.setAvatar(defaultAvatar);
				}
				// Update the existing person with the template data
				existingPerson.setEmail(person.getEmail());
				existingPerson.setAvatar(person.getAvatar());
				// TODO: what to update?

				entityManager.merge(existingPerson);
			}

			return Response.ok(person.getIdentity()).build();
		} catch (Exception e) {
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

	/**
	 * HTTP Signature: DELETE people/{id} (in: -, out: text/plain) Deletes the
	 * person matching the given identity. This must not cascade deletion to the
	 * recipes and ingredient types owned by the matching person, but set their
	 * respective owner to null. Only Admintrators or the matching person may
	 * perform this operation.
	 */
	@DELETE
	@Path("{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response deletePerson(@PathParam("id") @Positive long id) {
		try {
			Person person = entityManager.find(Person.class, id);
			if (person == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}

			if (!isAdministrator(id) && id != person.getIdentity()) {
				return Response.status(Response.Status.FORBIDDEN)
						.entity("Only administrators or the person itself may perform this operation.").build();
			}

			// Dissociate associated recipes and ingredient types
			person.getRecipes().forEach(recipe -> recipe.setOwner(null));
			person.getIngredientTypes().forEach(ingredientType -> ingredientType.setOwner(null));

			entityManager.remove(person);
			return Response.ok().build();
		} catch (Exception e) {
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

	/**
	 * HTTP Signature: GET people/{id} (in:- out: application/json) Returns the
	 * person matching the given identity.
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findPerson(@PathParam("id") @Positive long id) {
		try {
			Person person = entityManager.find(Person.class, id);
			if (person == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			} else {
				return Response.ok(person).build();
			}
		} catch (Exception e) {
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

	/**
	 * HTTP Signature: GET people/requester (in:- out: application/json) Returns the
	 * person matching the given header field “X-Requester-Identity”. Note that this
	 * header field is injected during successful authentication.
	 */
	@GET
	@Path("requester")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findRequester(@HeaderParam("X-Requester-Identity") @Positive long requesterIdentity) {
		try {
			Person person = entityManager.find(Person.class, requesterIdentity);
			if (person == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			} else {
				return Response.ok(person).build();
			}
		} catch (Exception e) {
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

	/**
	 * HTTP Signature: GET people/{id}/recipes (in:- out: application/json) Returns
	 * the recipes associated with an owner matching the given identity, sorted by
	 * ID.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}/recipes")
	public Response getRecipes(@PathParam("id") @Positive long id) {
		try {
			Person person = entityManager.find(Person.class, id);
			if (person == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			List<Recipe> recipes = person.getRecipes().stream().sorted(Comparator.comparing(Recipe::getIdentity))
					.collect(Collectors.toList());
			return Response.ok(recipes).build();
		} catch (Exception e) {
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

	/**
	 * HTTP Signature: GET people/{id}/ingredient-types (in:- out: application/json)
	 * Returns the ingredient types associated with an owner matching the given
	 * identity, sorted by ID.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}/ingredient-types")
	public Response getIngredientTypes(@PathParam("id") @Positive long id) {
		try {
			Person person = entityManager.find(Person.class, id);
			if (person == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			List<IngredientType> ingredientTypes = person.getIngredientTypes().stream()
					.sorted(Comparator.comparing(IngredientType::getIdentity)).collect(Collectors.toList());
			return Response.ok(ingredientTypes).build();
		} catch (Exception e) {
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

	// Implement other methods as per requirements
	private boolean isAdministrator(long requesterIdentity) {
		// TODO: check if the requester is an administrator
		return true;
	}

}
