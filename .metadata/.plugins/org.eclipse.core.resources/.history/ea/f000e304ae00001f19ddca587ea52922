package edu.sb.cookbook.persistence;

import java.util.Set;

import javax.json.bind.annotation.JsonbProperty;

public class Person {
	// TODO What needs to be added here?
	protected String DEFAULT_PASSWORD_HASH;

	private Document avatar;

	private Set<Recipe> recipes;

	private Set<IngredientType> ingredientTypes;

	private String email;

	private String passwordHash;

	static public enum group {
		USER, ADMIN
	}

	private Name name;

	private Address address;

	private Set<String> phones;

	public Person() {
		// TODO: add logic to constructor
		this.email = null;
	}
	
	@JsonbProperty
	public Document getAvatar () {
		return this.avatar;
	}
	
	public void setAvatar (final Document avatar) {
		this.avatar = avatar;
	}
	
	@JsonbProperty
	public Set<Recipe> getRecipes () {
		return this.recipes;
	}
	
	protected void setRecipes (final Set<Recipe> recipes) {
		this.recipes = recipes;
	}
	
	@JsonbProperty
	public Set<IngredientType> getIngredientTypes () {
		return this.ingredientTypes;
	}
	
	protected void setIngredientType (final Set<IngredientType> ingredientTypes) {
		this.ingredientTypes = ingredientTypes;
	}
	
	@JsonbProperty
	public String getEmail () {
		return this.email;
	}
	
	public void setEmail (final String email) {
		this.email = email;
	}
	
	@JsonbProperty
	public String getPasswordHash () {
		return this.passwordHash;
	}
	
	public void setPasswordHash (final String passwordHash) {
		this.passwordHash = passwordHash;
	}
	
	// TODO: add methods getGropus and setGroups
	
	@JsonbProperty
	public Name getName () {
		return this.name;
	}
	
	protected void setName (final Name name) {
		this.name = name;
	}
	
	@JsonbProperty
	public Address getAddress () {
		return this.address;
	}
	
	protected void setAddress (final Address address) {
		this.address = address;
	}
	
	@JsonbProperty
	public Set<String> getPhones () {
		return this.phones;
	}
	
	protected void setPhones (final Set<String> phones) {
		this.phones = phones;
	}
}
