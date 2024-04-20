package edu.sb.cookbook.persistence;

import javax.json.bind.annotation.JsonbProperty;

public class Recipe {
	private Document avatar;
	
	private Person owner;
	
	private Ingredient[] ingredients;
	
	private Document[] illustrations;
	
	private String title;
	
	private String description;
	
	private String instruction;
	
	static public enum category {
		MAIN_COURSE, APPETIZER, SNACK, DESSERT, BREAKFAST, BUFFET, BARBEQUE, ADOLESCENT, INFANT		
	}
	
	public Recipe() {
		avatar = new Document();
		owner = new Person();
	}
	
	
	/**
	 * Returns the avatar.
	 * @return the avatar, or {@code null} for none
	 */
	@JsonbProperty
	public Document getAvatar () {
		return this.avatar;
	}
	
	/**
	 * Sets the avatar.
	 * @param avatar the avatar, or {@code null} for none
	 */
	public void setAvatar (final Document avatar) {
		this.avatar = avatar;
	}
	
	/**
	 * Returns the owner.
	 * @return the owner, or {@code null} for none
	 */
	@JsonbProperty
	public Person getOwner () {
		return this.owner;
	}
	
	/**
	 * Sets the owner.
	 * @param owner the owner, or {@code null} for none
	 */
	public void setOwner (final Person owner) {
		this.owner = owner;
	}
	
	/**
	 * Returns the ingredients.
	 * @return the ingredients, or {@code null} for none
	 */
	@JsonbProperty
	public Ingredient[] getIngredients () {
		return this.ingredients;
	}
	
	/**
	 * Sets the owner.
	 * @param owner the owner, or {@code null} for none
	 */
	protected void setIngredients (final Ingredient[] ingredients) {
		this.ingredients = ingredients;
	}
	
	/**
	 * Returns the illustrations.
	 * @return the illustrations, or {@code null} for none
	 */
	@JsonbProperty
	public Document[] getIllustrations () {
		return this.illustrations;
	}
	
	/**
	 * Sets the owner.
	 * @param owner the owner, or {@code null} for none
	 */
	public void setIllustrations (final Document[] illustrations) {
		this.illustrations = illustrations;
	}
}
