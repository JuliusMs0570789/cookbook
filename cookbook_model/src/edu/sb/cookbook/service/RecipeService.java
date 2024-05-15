package edu.sb.cookbook.service;

import static edu.sb.cookbook.service.BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
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
import edu.sb.cookbook.persistence.Recipe;
import edu.sb.cookbook.persistence.Person.Group;
import edu.sb.tool.RestJpaLifecycleProvider;

@Path("recipes")
public class RecipeService {
	static private final String QUERY_RECIPES = "select recipe from Recipe as r where"
			+ "(:lowerNumber is null or r.number >= :lowerNumber) and "
			+ "(:upperNumber is null or r.number <= :upperNumber) and "
			+ "(:title is null or r.title = :title) and "
			+ "(:description is null or r.description = :description) and "
			+ "(:instruction is null or r.instruction like :instruction)";
	
	/**
	 * HTTP Signature: GET recipes IN: - OUT: application/json
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Recipe[] queryRecipes (
			@QueryParam("result-offset") @PositiveOrZero Integer resultOffset,
			@QueryParam("result-limit") @PositiveOrZero Integer resultLimit,
			@QueryParam("lower-number") final String lowerNumber,
			@QueryParam("upper-number") final String upperNumber,
			@QueryParam("title") final String title,
			@QueryParam("description") final String description,
			@QueryParam("instruction") final String instruction
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final TypedQuery<Long> query = entityManager.createQuery(QUERY_RECIPES, Long.class);
		
		if (resultOffset != null) query.setFirstResult(resultOffset);
		if (resultLimit != null) query.setMaxResults(resultLimit);
		
		final Recipe[] recipes = query
				.setParameter("lowerNumber", lowerNumber)
				.setParameter("upperNumber", upperNumber)
				.setParameter("title", title)
				.setParameter("description", description)
				.setParameter("instruction", instruction)
				.getResultList()
				.stream()
				.map(identity -> entityManager.find(Recipe.class, identity))
				.filter(recipe -> recipe != null)
				.sorted()
				.toArray(Recipe[]::new);

		return recipes;
	}
	
	/**
	 * HTTP Signature: POST recipes IN: application/json OUT: text/plain
	 * @param requesterIdentity the requester identity
	 * @param recipeTemplate the recipe template
	 * @return the recipe identity
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public long createOrUpdateRecipe (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@NotNull @Valid final Recipe recipeTemplate
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(Status.FORBIDDEN);
		final boolean insertMode = recipeTemplate.getIdentity() == 0L;
		
		final Recipe recipe;
		final Document avatar;
		if (insertMode) {
			recipe = new Recipe();
			recipe.setOwner(requester);
			avatar = entityManager.find(Document.class, recipeTemplate.getAvatar() == null ? 1L : recipeTemplate.getAvatar().getIdentity());
		} else {
			recipe = entityManager.find(Recipe.class, recipeTemplate.getIdentity());
			if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);
			avatar = recipeTemplate.getAvatar() == null ? recipe.getAvatar() : entityManager.find(Document.class, recipeTemplate.getAvatar().getIdentity());
		}
		
		if (requester.getGroup() != Group.ADMIN && recipe.getOwner() != requester) throw new ClientErrorException(Status.FORBIDDEN);
		if (avatar == null) throw new ClientErrorException(Status.NOT_FOUND);

		recipe.setModified(System.currentTimeMillis());
		recipe.setVersion(recipeTemplate.getVersion());
		recipe.setCategory(recipeTemplate.getCategory());
		recipe.setTitle(recipeTemplate.getTitle());
		recipe.setDescription(recipeTemplate.getDescription());
		recipe.setInstruction(recipeTemplate.getInstruction());
		recipe.setAvatar(avatar);

		try {
			if (insertMode)
				entityManager.persist(recipe);
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

		// 2nd level cache eviction if necessary
		final Cache secondLevelCache = entityManager.getEntityManagerFactory().getCache();
		if (insertMode) secondLevelCache.evict(Person.class, requester.getIdentity());

		return recipe.getIdentity();
	}
	
	/**
	 * HTTP Signature: DELETE recipes/{id} IN: - OUT: text/plain
	 * @param requesterIdentity the requester identity
	 * @param recipeIdentity the recipe identity
	 * @return the recipe identity
	 */
	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{id}")
	public long removeRecipe (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @Positive final long recipeIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(Status.FORBIDDEN);
		
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);
		if (requester.getGroup() != Group.ADMIN && requester != recipe.getOwner()) throw new ClientErrorException(Status.FORBIDDEN);

		try {
			entityManager.remove(recipe);
			entityManager.getTransaction().commit();
		} catch (final Exception e) {
			if (entityManager.getTransaction().isActive())
				entityManager.getTransaction().rollback();
			throw new ClientErrorException(Status.CONFLICT, e);
		} finally {
			entityManager.getTransaction().begin();
		}

		// 2nd level cache eviction if necessary
		final Cache secondLevelCache = entityManager.getEntityManagerFactory().getCache();
		secondLevelCache.evict(Person.class, requester.getIdentity());
		secondLevelCache.evict(Ingredient.class);
		secondLevelCache.evict(IngredientType.class);

		return recipe.getIdentity();
	}
	
	/**
	 * HTTP Signature: GET recipes/{id} IN: - OUT: application/json
	 * @param ingredientTypeIdentity the ingredient type identity
	 * @return the recipe
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public Recipe findRecipe (
		@PathParam("id") @Positive final long recipeIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);

		return recipe;
	}
	
	/**
	 * HTTP Signature: GET recipes/{id}/illustrations IN: - OUT: application/json
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}/illustrations")
	public Set<Document> getIllustrations (
		@PathParam("id") @Positive final long recipeIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);

		return recipe.getIllustrations();
	}
	
	/**
	 * HTTP Signature: GET recipes/{id}/ingredients IN: - OUT: application/json
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}/ingredients")
	public Set<Ingredient> getIngredients (
		@PathParam("id") @Positive final long recipeIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);
		
		return recipe.getIngredients();
	}
	
	/**
	 * HTTP Signature: POST recipes/{id}/illustrations IN: application/json OUT: text/plain
	 * @param illustration the illustration
	 * @return the recipe identity
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{id}/illustrations")
	public long addIllustration (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @Positive final long recipeIdentity,
		@QueryParam("illustrations") final long[] documentIllustrations
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(Status.FORBIDDEN);
		
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);
		if (requester.getGroup() != Group.ADMIN && requester != recipe.getOwner()) throw new ClientErrorException(Status.FORBIDDEN);
		
		final Set<Document> illustrations = LongStream
			.of(documentIllustrations)
			.mapToObj(ref -> entityManager.find(Document.class, ref))
			.filter(document -> document != null)
			.collect(Collectors.toSet());
		
		recipe.getIllustrations().retainAll(illustrations);
		recipe.getIllustrations().addAll(illustrations);

		try {
			entityManager.flush();
			entityManager.getTransaction().commit();
		} finally {
			entityManager.getTransaction().begin();
		}
		
		return recipe.getIdentity();
	}
	
	/**
	 * HTTP Signature: POST recipes/{id}/ingredients IN: application/json OUT: text/plain
	 * @param ingredient the ingredient
	 * @return the recipe identity
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{id}/ingredients")
	public long addIngredient (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @Positive final long recipeIdentity,
		@QueryParam("ingredients") final long[] ingredientsParam
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(Status.FORBIDDEN);
		
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);
		if (requester.getGroup() != Group.ADMIN && requester != recipe.getOwner()) throw new ClientErrorException(Status.FORBIDDEN);
		
		final Set<Ingredient> ingredients = LongStream
			.of(ingredientsParam)
			.mapToObj(ref -> entityManager.find(Ingredient.class, ref))
			.filter(ingredient -> ingredient != null)
			.collect(Collectors.toSet());
		
		recipe.getIngredients().retainAll(ingredients);
		recipe.getIngredients().addAll(ingredients);

		try {
			entityManager.flush();
			entityManager.getTransaction().commit();
		} finally {
			entityManager.getTransaction().begin();
		}
		
		return recipe.getIdentity();
	}
	
	/**
	 * HTTP Signature: DELETE recipes/{id1}/illustrations/{id2} IN: - OUT: text/plain
	 * @param id1 the recipeIdentity
	 * @param id2 the illustrationIdentity
	 * @return the Recipe identity
	 */
	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{id1}/illustrations/{id2}")
	public long dissacociateIllustration (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id1") @Positive final long recipeIdentity,
		@PathParam("id2") @Positive final long illustrationIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(Status.FORBIDDEN);
		
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);
		if (requester.getGroup() != Group.ADMIN && requester != recipe.getOwner()) throw new ClientErrorException(Status.FORBIDDEN);
		
		long[] illustrationIds = recipe.getIllustrations().stream()
                .mapToLong(Document::getIdentity)
                .toArray();
		
		final Set<Document> illustrationsToKeep = LongStream
				.of(illustrationIds)
				.mapToObj(ref -> entityManager.find(Document.class, ref))
				.filter(document -> document.getIdentity() != illustrationIdentity)
				.collect(Collectors.toSet());
			
			recipe.getIllustrations().retainAll(illustrationsToKeep);
			recipe.getIllustrations().addAll(illustrationsToKeep);
		

		return recipe.getIdentity();
	}
	
	/**
	 * HTTP Signature: DELETE recipes/{id1}/ingredients/{id2} IN: - OUT: text/plain
	 * @return the recipe identity
	 */
	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{id1}/ingredients/{id2}")
	public long removeIngredient (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id1") @Positive final long recipeIdentity,
		@PathParam("id2") @Positive final long illustrationIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(Status.FORBIDDEN);
		
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);
		if (requester.getGroup() != Group.ADMIN && requester != recipe.getOwner()) throw new ClientErrorException(Status.FORBIDDEN);
		
		long[] ingredientIds = recipe.getIngredients().stream()
                .mapToLong(Ingredient::getIdentity)
                .toArray();
		
		final Set<Document> illustrationsToKeep = LongStream
				.of(ingredientIds)
				.mapToObj(ref -> entityManager.find(Document.class, ref))
				.filter(document -> document.getIdentity() != illustrationIdentity)
				.collect(Collectors.toSet());
			
			recipe.getIllustrations().retainAll(illustrationsToKeep);
			recipe.getIllustrations().addAll(illustrationsToKeep);

		return recipe.getIdentity();
	}
}
