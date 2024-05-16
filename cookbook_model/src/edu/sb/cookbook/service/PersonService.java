package edu.sb.cookbook.service;

import static edu.sb.cookbook.service.BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import edu.sb.cookbook.persistence.Document;
import edu.sb.cookbook.persistence.Ingredient;
import edu.sb.cookbook.persistence.IngredientType;
import edu.sb.cookbook.persistence.Person;
import edu.sb.cookbook.persistence.Person.Group;
import edu.sb.cookbook.persistence.Recipe;
import edu.sb.tool.RestJpaLifecycleProvider;

@Path("persons")
public class PersonService {
	/**
	 * HTTP Signature: GET people IN: - OUT: application/json Returns the people
	 * matching the given filter criteria, with missing parameters identifying
	 * omitted criteria. Search criteria should be query parameters containing any
	 * “normal” property of both people and their composites, except identity and
	 * passwordHash, plus int values for result-offset and result-limit which define
	 * a paging range. The JPA query should return the matching Person-IDs, which
	 * are used subsequenty to query the people from the 2nd level cache using
	 * "entityManager.find()", and then sorted by name and email → best use
	 * Collection-Streams for the latter two steps.
	 */
	static private final String QUERY_PEOPLE = "SELECT p.identity FROM Person AS p WHERE "
			+ "(:maxCreated is null or p.created <= :maxCreated) and "
			+ "(:minModified is null or p.modified >= :minModified) and "
			+ "(:maxModified is null or p.modified <= :maxModified) and " + "(:email is null or p.email = :email) AND "
			+ "(:group is null or p.group = :group) AND" + "(:title is null or p.name.title = :title) AND "
			+ "(:givenName is null or p.name.given = :givenName) AND "
			+ "(:familyName is null or p.name.family = :familyName) AND "
			+ "(:street is null or p.address.street = :street) AND "
			+ "(:postcode is null or p.address.postcode = :postcode) AND "
			+ "(:city is null or p.address.city = :city) AND "
			+ "(:country is null or p.address.country = :country) AND "
			+ "(:phones is null or :phones MEMBER OF p.phones) AND "
			+ "(:recipes is null or :recipes MEMBER OF p.recipes) AND "
			+ "(:ingredientTypes is null or :ingredientTypes MEMBER OF p.ingredientTypes)";

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Person[] queryPeople(@QueryParam("result-offset") @PositiveOrZero final Integer resultOffset,
			@QueryParam("result-size") @PositiveOrZero final Integer resultSize,
			@QueryParam("min-created") final Long minCreated, @QueryParam("max-created") final Long maxCreated,
			@QueryParam("min-modified") final Long minModified, @QueryParam("max-modified") final Long maxModified,
			@QueryParam("email") final String email, @QueryParam("group") final Group group,
			@QueryParam("title") final String title, @QueryParam("givenName") final String givenName,
			@QueryParam("familyName") final String familyName, @QueryParam("street") final String street,
			@QueryParam("postcode") final String postcode, @QueryParam("city") final String city,
			@QueryParam("country") final String country, @QueryParam("phones") final List<String> phones,
			@QueryParam("recipes") final List<Recipe> recipes,
			@QueryParam("ingredientTypes") final List<IngredientType> ingredientTypes) {

		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final TypedQuery<Long> query = entityManager.createQuery(QUERY_PEOPLE, Long.class);
		if (resultOffset != null)
			query.setFirstResult(resultOffset);
		if (resultSize != null)
			query.setMaxResults(resultSize);
		query.setParameter("resultOffset", resultOffset);
		query.setParameter("resultSize", resultSize);
		query.setParameter("minCreated", minCreated);
		query.setParameter("maxCreated", maxCreated);
		query.setParameter("minModified", minModified);
		query.setParameter("maxModified", maxModified);
		query.setParameter("email", email);
		query.setParameter("group", group);
		query.setParameter("title", title);
		query.setParameter("givenName", givenName);
		query.setParameter("familyName", familyName);
		query.setParameter("street", street);
		query.setParameter("postcode", postcode);
		query.setParameter("city", city);
		query.setParameter("country", country);
		query.setParameter("phones", phones);
		query.setParameter("recipes", recipes);
		query.setParameter("ingredientTypes", ingredientTypes);

		final List<Person> people = query.getResultList().stream()
				.map(identity -> entityManager.find(Person.class, identity)).filter(type -> type != null)
				.sorted((p1, p2) -> {
					// Compare by family name, then given name, then email
					int familyComparison = p1.getName().getFamily().compareToIgnoreCase(p2.getName().getFamily());
					if (familyComparison != 0) {
						return familyComparison;
					}
					int givenComparison = p1.getName().getGiven().compareToIgnoreCase(p2.getName().getGiven());
					if (givenComparison != 0) {
						return givenComparison;
					}
					return p1.getEmail().compareToIgnoreCase(p2.getEmail());
				}).collect(Collectors.toList());

		return people.toArray(new Person[0]);
	}

	/**
	 * HTTP Signature: POST people IN: - JSON OUT: text/plain Inserts or updates a
	 * person from template data within the HTTP request body. It creates a new
	 * person if the given template's identity is zero, which solely admins may
	 * perform. Otherwise it updates the corresponding person with the given
	 * template data, which only adminstrators or the person itself may perform.
	 * Make sure non-administrators cannot upgrade their group. The default avatar
	 * with ID 1 shall be associated during creation if none is provided.
	 * Optionally, a new password may be set using the header field
	 * “X-Set-Password”. Returns the affected person's identity as text/plain. Only
	 * Admintrators or the given person may perform this operation.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public long createOrUpdatePerson(
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@NotNull @Valid final Person personTemplate
			) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(Status.FORBIDDEN);
		final boolean insertMode = personTemplate.getIdentity() == 0L;

		final Person person;
		final Document avatar;
		
		if (insertMode) {
			person = requester;
			avatar = entityManager.find(Document.class, personTemplate.getAvatar() == null ? 1L : personTemplate.getAvatar().getIdentity());
		} else {
			person = entityManager.find(Person.class, personTemplate.getIdentity());
			if (person == null) throw new ClientErrorException(Status.NOT_FOUND);
			avatar = personTemplate.getAvatar() == null ? person.getAvatar() : entityManager.find(Document.class, personTemplate.getAvatar().getIdentity());
		}
		
		if (requester.getGroup() != Group.ADMIN && person != requester) throw new ClientErrorException(Status.FORBIDDEN);
		if (avatar == null) throw new ClientErrorException(Status.NOT_FOUND);

		person.setModified(System.currentTimeMillis());
		person.setVersion(personTemplate.getVersion());
		person.setEmail(personTemplate.getEmail());
		person.setPasswordHash(personTemplate.getPasswordHash());
		person.setAvatar(personTemplate.getAvatar());
		
		try {
			if (insertMode)
				entityManager.persist(person);
			else
				entityManager.flush();

			entityManager.getTransaction().commit();
		} catch (final Exception e) {
			if (entityManager.getTransaction().isActive())
				entityManager.getTransaction().rollback();
			throw new ClientErrorException(Status.CONFLICT, e);
		} finally {
			entityManager.getTransaction().begin();
		}
		
		final Cache secondLevelCache = entityManager.getEntityManagerFactory().getCache();
		if (insertMode) secondLevelCache.evict(Person.class, requester.getIdentity());

		return person.getIdentity();
	}

	/**
	 * HTTP Signature: DELETE people/{id} IN: - OUT: text/plain Deletes the person
	 * matching the given identity. This must not cascade deletion to the recipes
	 * and ingredient types owned by the matching person, but set their respective
	 * owner to null. Only Admintrators or the matching person may perform this
	 * operation.
	 */
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{id}")
	public long deletePerson(@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@PathParam("id") @Positive final long personIdentity) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null)
			throw new ClientErrorException(Status.FORBIDDEN);

		final Person person = entityManager.find(Person.class, personIdentity);
		if (person == null)
			throw new ClientErrorException(Status.NOT_FOUND);
		if (requester.getGroup() != Group.ADMIN && requester != person)
			throw new ClientErrorException(Status.FORBIDDEN);

		try {
			entityManager.remove(person);

			entityManager.getTransaction().commit();
		} catch (final Exception e) {
			if (entityManager.getTransaction().isActive())
				entityManager.getTransaction().rollback();
			throw new ClientErrorException(Status.CONFLICT, e);
		} finally {
			entityManager.getTransaction().begin();
		}

		// TODO: How to handle for person?
		final Cache secondLevelCache = entityManager.getEntityManagerFactory().getCache();
		secondLevelCache.evict(Person.class, requester.getIdentity());
		secondLevelCache.evict(Ingredient.class);
		secondLevelCache.evict(Recipe.class);

		return person.getIdentity();
	}

	/**
	 * HTTP Signature: GET people/{id} IN: - OUT: application/json Returns the
	 * person matching the given identity.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public void findPerson() {
		// TODO
	}

	/**
	 * HTTP Signature: GET people/requester IN: - OUT: application/json Returns the
	 * person matching the given header field “X-Requester-Identity”. Note that this
	 * header field is injected during successful authentication.
	 */
	@GET
	@Path("{requester}")
	@Produces(MediaType.APPLICATION_JSON)
	public Person findRequester(@PathParam("requester") @Positive final long personIdentity) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Person person = entityManager.find(Person.class, personIdentity);
		if (person == null)
			throw new ClientErrorException(Status.NOT_FOUND);

		return person;
	}

	/**
	 * HTTP Signature: GET people/{id}/recipes IN: - OUT: application/json Returns
	 * the recipes associated with an owner matching the given identity, sorted by
	 * ID.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}/recipes")
	public void getRecipes() {
		// TODO
	}

	/**
	 * HTTP Signature: GET people/{id}/ingredient-types IN: - OUT: application/json
	 * Returns the ingredient types associated with an owner matching the given
	 * identity, sorted by ID.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}/ingredient-types")
	public void getIngredientTypes() {
		// TODO
	}
}