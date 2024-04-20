package edu.sb.cookbook.persistence;

import javax.json.bind.annotation.JsonbProperty;
import javax.persistence.Column;

public class Ingredient {	
	@Column(nullable=false, updatable=true)
	private Recipe recipe;
	
	private IngredientType type;
	
	private float amount;
	
	static public enum unit {
		LITRE, GRAM, TEASPOON, TABLESPOON, PINCH, CUP, CAN, TUBE, BUSHEL, PIECE		
	}
	
	public Ingredient () {
		// TODO: how can the recipe be initialized at first?
		// this.recipe = ...;
		this.type = null;
		this.amount = 0;
	}
	
	@JsonbProperty
	public Recipe getRecipe () {
		return this.recipe;
	}
	
	protected void setRecipe (final Recipe recipe) {
		this.recipe = recipe;
	}
	
	@JsonbProperty
	public IngredientType getType () {
		return this.type;
	}
	
	public void setType (final IngredientType type) {
		this.type = type;
	}
}
