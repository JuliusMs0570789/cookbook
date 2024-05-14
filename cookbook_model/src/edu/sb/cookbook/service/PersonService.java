package edu.sb.cookbook.service;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("persons")
public class PersonService {
	/**
	 * HTTP Signature: GET people IN: - OUT: application/json
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public void queryPeople () {
		// TODO
	}
	
	/**
	 * HTTP Signature: POST people IN: - JSON OUT: text/plain
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public void createOrUpdatePerson () {
		// TODO
	}
	
	/**
	 * HTTP Signature: DELETE people/{id} IN: - OUT: text/plain
	 */
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("{id}")
	public void deletePerson () {
		// TODO
	}
	
	/**
	 * HTTP Signature: GET people/{id} IN: - OUT: application/json
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public void findPerson () {
		// TODO
	}
	
	/**
	 * HTTP Signature: GET people/requester IN: - OUT: application/json
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("requester")
	public void findRequester () {
		// TODO
	}
	
	/**
	 * HTTP Signature: GET people/{id}/recipes IN: - OUT: application/json
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}/recipes")
	public void getRecipes () {
		// TODO
	}
	
	/**
	 * HTTP Signature: GET people/{id}/ingredient-types IN: - OUT: application/json
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}/ingredient-types")
	public void getIngredientTypes () {
		// TODO
	}
}
