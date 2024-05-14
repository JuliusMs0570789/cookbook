package edu.sb.cookbook.service;

import static edu.sb.cookbook.service.BasicAuthenticationReceiverFilter.REQUESTER_IDENTITY;

import javax.validation.constraints.Positive;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

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
	 * @return the recipe identity
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public long createOrInsertRecipe () {
		// TODO
		return 15l;
	}
	
	/**
	 * HTTP Signature: DELETE recipes/{id} IN: - OUT: text/plain
	 */
	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{id}")
	public void removeRecipe (
		@HeaderParam(REQUESTER_IDENTITY) @Positive final long requesterIdentity,
		@PathParam("id") @Positive final long recipeIdentity
	) {
		// TODO
	}
	
	/**
	 * HTTP Signature: GET recipes/{id} IN: - OUT: application/json
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public void findRecipe (
		@PathParam("id") @Positive final long recipeIdentity
	) {
		// TODO
	}
	
	/**
	 * HTTP Signature: GET recipes/{id}/illustrations IN: - OUT: application/json
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}/illustrations")
	public void getIllustrations (
		@PathParam("id") @Positive final long recipeIdentity
	) {
		// TODO
	}
	
	/**
	 * HTTP Signature: GET recipes/{id}/ingredients IN: - OUT: application/json
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}/ingredients")
	public void getIngredients (
		@PathParam("id") @Positive final long recipeIdentity
	) {
		// TODO
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
		// TODO
		return 15l;
	}
}
