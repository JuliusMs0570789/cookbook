package edu.sb.cookbook.service;

import static edu.sb.cookbook.service.BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY;
import java.util.Objects;
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
			+ "(:minCreated is null or r.created >= :minCreated) and "
			+ "(:maxCreated is null or r.created <= :maxCreated) and "
			+ "(:minModified is null or r.modified >= :minModified) and "
			+ "(:maxModified is null or r.modified <= :maxModified) and "
			+ "(:category is null or r.category = :category) and "
			+ "(:title is null or r.title = :title) and "
			+ "(:descriptionFragment is null or r.description like concat('%', :descriptionFragment, '%') and "
			+ "(:instructionFragment is null or r.instruction like ('%', :instructionFragment, '%')) and "
			+ "(:minIngredientCount is null or size(r.ingredients) >= :minIngredientCount) and "
			+ "(:maxIngredientCount is null or size(r.ingredients) <= :maxIngredientCount)";
	
	/**
	 * HTTP Signature: GET recipes IN: - OUT: application/json
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Recipe[] queryRecipes (
			@QueryParam("result-offset") @PositiveOrZero Integer resultOffset,
			@QueryParam("result-limit") @PositiveOrZero Integer resultLimit,
			@QueryParam("min-created") final Long minCreated,
			@QueryParam("max-created") final Long maxCreated,
			@QueryParam("min-modified") final Long minModified,
			@QueryParam("max-modified") final Long maxModified,
			@QueryParam("title") final String title,
			@QueryParam("description-fragment") final String descriptionFragment,
			@QueryParam("instruction-fragment") final String instructionFragment,
			@QueryParam("min-ingredient-count") final String minIngredientCount,
			@QueryParam("max-ingredient-count") final String maxIngredientCount
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final TypedQuery<Long> query = entityManager.createQuery(QUERY_RECIPES, Long.class);
		
		if (resultOffset != null) query.setFirstResult(resultOffset);
		if (resultLimit != null) query.setMaxResults(resultLimit);
		
		final Recipe[] recipes = query
			.setParameter("minCreated", minCreated)
			.setParameter("maxCreated", maxCreated)
			.setParameter("minModified", minModified)
			.setParameter("maxModified", maxModified)
			.setParameter("title", title)
			.setParameter("descriptionFragment", descriptionFragment)
			.setParameter("instructionFragment", instructionFragment)
			.setParameter("minIngredientCount", minIngredientCount)
			.setParameter("maxgredientCount", maxIngredientCount)	
			.getResultList()
			.stream()
			.map(identity -> entityManager.find(Recipe.class, identity))
			.filter(Objects::nonNull)
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
			if (avatar == null) throw new IllegalStateException();
		} else {
			recipe = entityManager.find(Recipe.class, recipeTemplate.getIdentity());
			if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);
			avatar = recipeTemplate.getAvatar() == null ? recipe.getAvatar() : entityManager.find(Document.class, recipeTemplate.getAvatar().getIdentity());
			if (avatar == null) throw new ClientErrorException(Status.NOT_FOUND);
		}
		
		if (requester.getGroup() != Group.ADMIN && recipe.getOwner() != requester) throw new ClientErrorException(Status.FORBIDDEN);

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
			for(final Ingredient ingredient : recipe.getIngredients()) {
				entityManager.remove(ingredient);
			}
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
	public Document[] getIllustrations (
		@PathParam("id") @Positive final long recipeIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);

		final Document[] illustrations = recipe.getIllustrations()
				.stream()
				.sorted()
				.toArray(Document[]::new);
		
		return illustrations;
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
		@NotNull @Valid final Document illustrationTemplate
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(Status.FORBIDDEN);
		
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);
		if (requester.getGroup() != Group.ADMIN && requester != recipe.getOwner()) throw new ClientErrorException(Status.FORBIDDEN);
		
		final Document illustration = entityManager.find(Document.class, illustrationTemplate.getIdentity());
		if (illustration == null) throw new ClientErrorException(Status.NOT_FOUND);

		recipe.setModified(System.currentTimeMillis());
		recipe.getIllustrations().add(illustration);
		
		try {
			entityManager.flush();
			
			entityManager.getTransaction().commit();
		} catch (final Exception e) {
			if (entityManager.getTransaction().isActive())
				entityManager.getTransaction().rollback();
			throw new ClientErrorException(Status.CONFLICT, e);
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
	@Path("{id1}/illustrations/{id2}")
	@Produces(MediaType.TEXT_PLAIN)
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
		
		recipe.getIllustrations().removeIf(illustration -> illustration.getIdentity() == illustrationIdentity);
	
		try {
			entityManager.flush();
			
			entityManager.getTransaction().commit();
		} catch (final Exception e) {
			if (entityManager.getTransaction().isActive())
				entityManager.getTransaction().rollback();
			throw new ClientErrorException(Status.CONFLICT, e);
		} finally {
			entityManager.getTransaction().begin();
		}

		return recipe.getIdentity();
	}
	
	
	/**
	 * HTTP Signature: GET recipes/{id}/ingredients IN: - OUT: application/json
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}/ingredients")
	public Ingredient[] getIngredients (
		@PathParam("id") @Positive final long recipeIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);
		
		final Ingredient[] ingredients = recipe.getIngredients()
				.stream()
				.sorted()
				.toArray(Ingredient[]::new);
		
		return ingredients;
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
	public long insertOrUpdateIngredient (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @Positive final long recipeIdentity,
		@NotNull @Valid final Ingredient ingredientTemplate
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(Status.FORBIDDEN);
		
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);
		if (requester.getGroup() != Group.ADMIN && requester != recipe.getOwner()) throw new ClientErrorException(Status.FORBIDDEN);
		
		final boolean insertMode = ingredientTemplate.getIdentity() == 0L;

		final IngredientType ingredientType = entityManager.find(IngredientType.class, ingredientTemplate.getType().getIdentity());
		if (ingredientType == null) throw new ClientErrorException(Status.NOT_FOUND);
		
		final Ingredient ingredient;
		if (insertMode) {
			ingredient = new Ingredient(recipe);
		} else {
			ingredient = entityManager.find(Ingredient.class, ingredientTemplate.getIdentity());
			if(ingredient == null) throw new ClientErrorException(Status.NOT_FOUND);
		}
		
		ingredient.setModified(System.currentTimeMillis());
		ingredient.setVersion(ingredientTemplate.getVersion());
		ingredient.setAmount(ingredientTemplate.getAmount());
		ingredient.setUnit(ingredientTemplate.getUnit());
		ingredient.setType(ingredientType);

		try {
			if (insertMode) entityManager.persist(ingredient);
			else entityManager.flush();
			
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
		if (insertMode) secondLevelCache.evict(Recipe.class, recipe.getIdentity());
		
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
		@PathParam("id2") @Positive final long ingredientIdentity
	) {
		final EntityManager entityManager = RestJpaLifecycleProvider.entityManager("local_database");
		final Person requester = entityManager.find(Person.class, requesterIdentity);
		if (requester == null) throw new ClientErrorException(Status.FORBIDDEN);
		
		final Recipe recipe = entityManager.find(Recipe.class, recipeIdentity);
		if (recipe == null) throw new ClientErrorException(Status.NOT_FOUND);
		if (requester.getGroup() != Group.ADMIN && requester != recipe.getOwner()) throw new ClientErrorException(Status.FORBIDDEN);
		
		recipe.getIngredients().removeIf(ingredient -> ingredient.getIdentity() == ingredientIdentity);
		
		try {
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
		secondLevelCache.evict(Recipe.class, recipe.getIdentity());

		return recipe.getIdentity();
	}
}
