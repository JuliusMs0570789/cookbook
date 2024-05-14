package edu.sb.cookbook.service;

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
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public void createOrInsertRecipe () {
		// TODO
	}
	
	/**
	 * HTTP Signature: DELETE recipes/{id} IN: - OUT: text/plain
	 */
	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{id}")
	public void deleteRecipe () {
		// TODO
	}
	
	/**
	 * HTTP Signature: GET recipes/{id} IN: - OUT: application/json
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public void findRecipe () {
		// TODO
	}
	
	/**
	 * HTTP Signature: GET recipes/{id}/illustrations IN: - OUT: application/json
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}/illustrations")
	public void getIllustrations () {
		// TODO
	}
	
	/**
	 * HTTP Signature: GET recipes/{id}/ingredients IN: - OUT: application/json
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}/ingredients")
	public void getIngredients () {
		// TODO
	}
	
	/**
	 * HTTP Signature: POST recipes/{id}/illustrations IN: application/json OUT: text/plain
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{id}/illustrations")
	public void addIllustration () {
		// TODO
	}
	
	/**
	 * HTTP Signature: POST recipes/{id}/ingredients IN: application/json OUT: text/plain
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{id}/ingredients")
	public void addIngredient () {
		// TODO
	}
	
	/**
	 * HTTP Signature: DELETE recipes/{id1}/illustrations/{id2} IN: - OUT: text/plain
	 */
	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{id1}/illustrations/{id2}")
	public void dissacociateIllustration () {
		// TODO
	}
	
	/**
	 * HTTP Signature: DELETE recipes/{id1}/ingredients/{id2} IN: - OUT: text/plain
	 */
	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{id1}/ingredients/{id2}")
	public void removeIngredient () {
		// TODO
	}
}
