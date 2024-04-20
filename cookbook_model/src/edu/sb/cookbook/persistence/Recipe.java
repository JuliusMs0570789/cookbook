package edu.sb.cookbook.persistence;

import java.util.Set;
import javax.json.bind.annotation.JsonbProperty;

public class Recipe {
	private Document avatar;
	
	private Person owner;
	
	private Set<Ingredient> ingredients;
	
	private Set<Document> illustrations;
	
	private String title;
	
	private String description;
	
	private String instruction;
	
	static public enum category {
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
