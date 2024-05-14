package edu.sb.cookbook.service;

import static edu.sb.cookbook.service.BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY;

import java.util.Set;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
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
	/**
	 * HTTP Signature: GET recipes IN: - OUT: application/json
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public void queryRecipes () {
		// TODO
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

		// TODO: adapt to recipe fields (currently the setters are the ones from IngredientType)
		// recipe.setModified(System.currentTimeMillis());
		// recipe.setVersion(recipeTemplate.getVersion());
		// recipe.setAlias(recipeTemplate.getAlias());
		// recipe.setDescription(recipeTemplate.getDescription());
		// recipe.setRestriction(recipeTemplate.getRestriction());
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
	 * @param recipeIdentity the ingredient type identity
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
	 * @return the recipe identity
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{id}/illustrations")
	public long addIllustration (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @Positive final long recipeIdentity
	) {
		// TODO
		return 15l;
	}
	
	/**
	 * HTTP Signature: POST recipes/{id}/ingredients IN: application/json OUT: text/plain
	 * @return the recipe identity
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{id}/ingredients")
	public long addIngredient (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @Positive final long recipeIdentity
	) {
		// TODO
		return 15l;
	}
	
	/**
	 * HTTP Signature: DELETE recipes/{id1}/illustrations/{id2} IN: - OUT: text/plain
	 * @return the Document identity
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
		
		// TODO
		return 15l;
	}
	
	/**
	 * HTTP Signature: DELETE recipes/{id1}/ingredients/{id2} IN: - OUT: text/plain
	 * @return the Document identity
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
		
		// TODO
		return 15l;
	}
}
