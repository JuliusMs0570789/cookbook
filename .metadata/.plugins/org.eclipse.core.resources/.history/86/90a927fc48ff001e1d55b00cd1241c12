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
	
	/**
	 * Returns the title.
	 * @return the title, or {@code null} for none
	 */
	@JsonbProperty
	public String getTitle () {
		return this.title;
	}
	
	/**
	 * Sets the title.
	 * @param title the title, or {@code null} for none
	 */
	public void setTitle (final String title) {
		this.title = title;
	}
	
	/**
	 * Returns the description.
	 * @return the description, or {@code null} for none
	 */
	@JsonbProperty
	public String getDescription () {
		return this.description;
	}
	
	/**
	 * Sets the description.
	 * @param description the description, or {@code null} for none
	 */
	public void setDescription (final String description) {
		this.description = description;
	}
	
	/**
	 * Returns the instruction.
	 * @return the instruction, or {@code null} for none
	 */
	@JsonbProperty
	public String getInstruction () {
		return this.instruction;
	}
	
	/**
	 * Sets the instruction.
	 * @param instruction the instruction, or {@code null} for none
	 */
	public void setInstruction (final String instruction) {
		this.instruction = instruction;
	}
}
