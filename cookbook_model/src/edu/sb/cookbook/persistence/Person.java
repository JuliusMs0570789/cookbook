package edu.sb.cookbook.persistence;

import java.util.HashSet;
import java.util.Set;

import javax.json.bind.annotation.JsonbProperty;

// TODO: 1:1-Relation (composition) zu Address
//TODO: 1:1-Relation (composition) zu Name
// TODO: 0..1:* Relation (beidseitig) zu Recipe
// TODO: *:1 Relation zu Dokument
//TODO: 0..1:* Relation (beidseitig) zu IngredientType
//TODO: 1:* Relation zu Person.Group



public class Person extends BaseEntity {
	// TODO What needs to be added here?
	protected String DEFAULT_PASSWORD_HASH;

	private Document avatar;

	private Set<Recipe> recipes;

	private Set<IngredientType> ingredientTypes;

	private String email;

	private String passwordHash;
	
	private Person.Group group;
	
	private Name name;

	private Address address;

	private Set<String> phones;
	
	static public enum Group {
		USER, ADMIN
	}

	public Person() {
		// TODO: add logic to constructor
		this.recipes = new HashSet<>();
		this.ingredientTypes = new HashSet<>();
		this.email = null;
		this.group = Group.values()[0];
		this.phones = new HashSet<>();
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
	
	public Person.Group getGroup() {
		return this.group;
	}
	
	public void setGroup(Person.Group group) {
		this.group = group;
	}
	
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
