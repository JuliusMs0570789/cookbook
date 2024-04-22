package edu.sb.cookbook.persistence;

import java.util.Set;
import javax.json.bind.annotation.JsonbProperty;

// TODO: *:0..1 Relation (beidseitig) zu Person
// TODO: *:1 Relation (+avatar) zu Dokument
// TODO: *:1 Relation (+illustrations)  zu Dokument
// TODO: 1:* Relation (beidseitig) zu Ingredient
// TODO: *:1 Relation zu Recipe.Category
// TODO: dependency zu Restriction

public class Recipe extends BaseEntity {
	private Document avatar;
	
	private Person owner;
	
	private Set<Ingredient> ingredients;
	
	private Set<Document> illustrations;
	
	private Recipe.Category category;
	
	private String title;
	
	private String description;
	
	private String instruction;
	
	static public enum Category {
		MAIN_COURSE, APPETIZER, SNACK, DESSERT, BREAKFAST, BUFFET, BARBEQUE, ADOLESCENT, INFANT		
	}
	
	public Recipe() {
		this.avatar = null;
		this.owner = null;
		// TODO: how should Ingredients and Illustrations be initialized?
		// TODO: how should Category be initialized?
		this.title = null;
		this.description = null;
		this.instruction = null;	
	}
	
	
	@JsonbProperty
	public Document getAvatar () {
		return this.avatar;
	}
	
	public void setAvatar (final Document avatar) {
		this.avatar = avatar;
	}
	
	@JsonbProperty
	public Person getOwner () {
		return this.owner;
	}
	
	public void setOwner (final Person owner) {
		this.owner = owner;
	}
	
	@JsonbProperty
	public Set<Ingredient> getIngredients () {
		return this.ingredients;
	}
	
	protected void setIngredients (final Set<Ingredient> ingredients) {
		this.ingredients = ingredients;
	}
	
	@JsonbProperty
	public Set<Document> getIllustrations () {
		return this.illustrations;
	}
	
	public void setIllustrations (final Set<Document> illustrations) {
		this.illustrations = illustrations;
	}
	
	public Recipe.Category getCategory() {
		return this.category;
	}
	
	public void setCategory(Recipe.Category category) {
		this.category = category;
	}
	
	@JsonbProperty
	public String getTitle () {
		return this.title;
	}
	
	public void setTitle (final String title) {
		this.title = title;
	}
	
	@JsonbProperty
	public String getDescription () {
		return this.description;
	}
	
	public void setDescription (final String description) {
		this.description = description;
	}
	
	@JsonbProperty
	public String getInstruction () {
		return this.instruction;
	}
	
	public void setInstruction (final String instruction) {
		this.instruction = instruction;
	}
}
