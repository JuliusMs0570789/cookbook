package edu.sb.cookbook.service;

import static edu.sb.cookbook.service.BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
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

@Path("people")
public class PersonService {
	
	static public final Comparator<Person> PERSON_COMPARATOR = Comparator
            .comparing(Person::getName)
			.thenComparing(Person::getEmail);
	
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
			+ "(:minCreated is null or p.created >= :minCreated) AND "
			+ "(:maxCreated is null or p.created <= :maxCreated) AND "
			+ "(:minModified is null or p.modified >= :minModified) AND "
			+ "(:maxModified is null or p.modified <= :maxModified) AND "
			+ "(:email is null or p.email = :email) AND "
			+ "(:group is null or p.group = :group) AND "
			+ "(:title is null or p.name.title = :title) AND "
			+ "(:givenName is null or p.name.given = :givenName) AND "
			+ "(:familyName is null or p.name.family = :familyName) AND "
			+ "(:street is null or p.address.street = :street) AND "
			+ "(:postcode is null or p.address.postcode = :postcode) AND "
			+ "(:city is null or p.address.city = :city) AND "
			+ "(:country is null or p.address.country = :country)";

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Person[] queryPeople(
		@QueryParam("result-offset") @PositiveOrZero final Integer resultOffset,
		@QueryParam("result-size") @PositiveOrZero final Integer resultSize,
		@QueryParam("min-created") final Long minCreated,
		@QueryParam("max-created") final Long maxCreated,
		@QueryParam("min-modified") final Long minModified,
		@QueryParam("max-modified") final Long maxModified,
		@QueryParam("email") @Size(min=1, max=128) final String email,
		@QueryParam("group") final Group group,
		@QueryParam("title") final String title,
		@QueryParam("given-name") final String givenName,
		@QueryParam("family-name") final String familyName,
		@QueryParam("street") final String street,
		@QueryParam("postcode") final String postcode,
		@QueryParam("city") final String city,
		@QueryParam("country") final String country
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final TypedQuery<Long> query = entityManager.createQuery(QUERY_PEOPLE, Long.class);
		if (resultOffset != null) query.setFirstResult(resultOffset);
		if (resultSize != null) query.setMaxResults(resultSize);
		
		final List<Person> people = query
			.setParameter("minCreated", minCreated)
			.setParameter("maxCreated", maxCreated)
			.setParameter("minModified", minModified)
			.setParameter("maxModified", maxModified)
			.setParameter("email", email)
			.setParameter("group", group)
			.setParameter("title", title)
			.setParameter("givenName", givenName)
			.setParameter("familyName", familyName)
			.setParameter("street", street)
			.setParameter("postcode", postcode)
			.setParameter("city", city)
			.setParameter("country", country)
			.getResultList()
			.stream()
			.map(identity -> entityManager.find(Person.class, identity))
			.filter(Objects::nonNull)
			.sorted(PERSON_COMPARATOR)
			.collect(Collectors.toList());

		return people.toArray(Person[]::new);
	}

	/**
	 * HTTP Signature: POST people IN: - JSON OUT: text/plain Inserts or updates a
	 * person from template data within the HTTP request body. It creates a new
	 * person if the given template's identity is zero, which solely admins may
	 * perform. Otherwise it updates the corresponding person with the given
	 * template data, which only administrators or the person itself may perform.
	 * Make sure non-administrators cannot upgrade their group. The default avatar
	 * with ID 1 shall be associated during creation if none is provided.
	 * Optionally, a new password may be set using the header field
	 * “X-Set-Password”. Returns the affected person's identity as text/plain. Only
	 * Administrators or the given person may perform this operation.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public long createOrUpdatePerson(
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@NotNull @Valid final Person personTemplate, 
			@HeaderParam("X-Set-Password") @Size(min=1) final String newPassword
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		
		if (requester == null) throw new ClientErrorException(Status.FORBIDDEN);
		final boolean insertMode = personTemplate.getIdentity() == 0L;

		final Person person;
		final Document avatar;

		if (insertMode) {
			person = new Person();
			avatar = entityManager.find(Document.class, personTemplate.getAvatar() == null ? 1L : personTemplate.getAvatar().getIdentity());
			if (avatar == null) throw new IllegalStateException();
		} else {
			person = entityManager.find(Person.class, personTemplate.getIdentity());
			avatar = personTemplate.getAvatar() == null ? person.getAvatar() : entityManager.find(Document.class, personTemplate.getAvatar().getIdentity());
			if (avatar == null) throw new ClientErrorException(Status.NOT_FOUND);
		}

		if (requester.getGroup() != Group.ADMIN && person != requester) throw new ClientErrorException(Status.FORBIDDEN);

		person.setModified(System.currentTimeMillis());
		person.setVersion(personTemplate.getVersion());
		person.setEmail(personTemplate.getEmail());
		person.setAvatar(avatar);
		person.getName().setTitle(personTemplate.getName().getTitle());
		person.getName().setFamily(personTemplate.getName().getFamily());
		person.getName().setGiven(personTemplate.getName().getGiven());
		person.getAddress().setStreet(personTemplate.getAddress().getStreet());
		person.getAddress().setPostcode(personTemplate.getAddress().getPostcode());
		person.getAddress().setCity(personTemplate.getAddress().getCity());
		person.getAddress().setCountry(personTemplate.getAddress().getCountry());
		// TODO: phone number
		
		if (newPassword != null) person.setPasswordHash(newPassword);
	
		if (requester.getGroup() == Group.ADMIN) person.setGroup(personTemplate.getGroup());

		entityManager.getTransaction().begin();
		try {
			if (insertMode) {
				entityManager.persist(person);
			} else {
				entityManager.flush();
			}
			entityManager.getTransaction().commit();
		} catch (final Exception e) {
			if (entityManager.getTransaction().isActive()) entityManager.getTransaction().rollback();
			throw new ClientErrorException(Status.CONFLICT, e);
		}
		
		// if persons are inserted or updated, we don't need to care about the second level cache
		// because no mirror relationships changed

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
	public long deletePerson(
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
			@PathParam("id") @Positive final long personIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");

		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(Status.FORBIDDEN);

		final Person person = entityManager.find(Person.class, personIdentity);
		if (person == null) throw new ClientErrorException(Status.NOT_FOUND);
		if (requester.getGroup() != Group.ADMIN && requester != person) throw new ClientErrorException(Status.FORBIDDEN);

		try {
		    entityManager.getTransaction().begin();
		    // Set owner of recipes and ingredient types to null
		    person.getRecipes().forEach(recipe -> recipe.setOwner(null));
		    person.getIngredientTypes().forEach(type -> type.setOwner(null));

		    entityManager.remove(person);
		    entityManager.getTransaction().commit();
		} catch (final Exception e) {
		    if (entityManager.getTransaction().isActive()) entityManager.getTransaction().rollback();
		    throw new ClientErrorException(Status.CONFLICT, e);
		}

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
	public Person findPerson(@PathParam("id") @Positive final long personIdentity) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Person person = entityManager.find(Person.class, personIdentity);
		if (person == null) throw new ClientErrorException(Status.NOT_FOUND);

		return person;
	}

	/**
	 * HTTP Signature: GET people/requester IN: - OUT: application/json Returns the
	 * person matching the given header field “X-Requester-Identity”. Note that this
	 * header field is injected during successful authentication.
	 */
	@GET
	@Path("requester")
	@Produces(MediaType.APPLICATION_JSON)
	public Person findRequester(
			@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Person person = entityManager.find(Person.class, requesterIdentity);
		if (person == null) throw new ClientErrorException(Status.NOT_FOUND);

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
	public Recipe[] getRecipes(
			@PathParam("id") @Positive final long personIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Person person = entityManager.find(Person.class, personIdentity);
		if (person == null) throw new ClientErrorException(Status.NOT_FOUND);

		List<Recipe> recipes = person.getRecipes().stream()
				.sorted((r1, r2) -> Long.compare(r1.getIdentity(), r2.getIdentity())).collect(Collectors.toList());

		return recipes.toArray(Recipe[]::new);
	}

	/**
	 * HTTP Signature: GET people/{id}/ingredient-types IN: - OUT: application/json
	 * Returns the ingredient types associated with an owner matching the given
	 * identity, sorted by ID.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}/ingredient-types")
	public IngredientType[] getIngredientTypes(
			@PathParam("id") @Positive final long personIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Person person = entityManager.find(Person.class, personIdentity);
		if (person == null) throw new ClientErrorException(Status.NOT_FOUND);

		List<IngredientType> ingredientTypes = person
				.getIngredientTypes()
				.stream()
				.sorted((i1, i2) -> Long.compare(i1.getIdentity(), i2.getIdentity())).collect(Collectors.toList());

		return ingredientTypes.toArray(IngredientType[]::new);
	}
}
