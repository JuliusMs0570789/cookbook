package edu.sb.cookbook.persistence;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import edu.sb.tool.JsonProtectedPropertyStrategy;

@Entity
@Table(schema="cookbook", name="Ingredient", indexes={})
@PrimaryKeyJoinColumn(name="ingredientIdentity")
@DiscriminatorValue("Ingredient")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
public class Ingredient extends BaseEntity {	
	static public enum Unit {
		LITRE, GRAM, TEASPOON, TABLESPOON, PINCH, CUP, CAN, TUBE, BUSHEL, PIECE		
	}
	
	@PositiveOrZero
	@Column(nullable=false, updatable=true)
	private float amount;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable=false, updatable=true)
	private Unit unit;
	
	@ManyToOne(optional = false)
	@JoinColumn(nullable = false, updatable = false, insertable = true, name="recipeReference")
	private Recipe recipe;
	
	@ManyToOne(optional = false)
	@JoinColumn(nullable = false, updatable = true, name="typeReference")
	private IngredientType type;
	
	protected Ingredient () {
		this(null);
	}

	public Ingredient (Recipe recipe) {
		super();
		this.recipe = recipe;
		this.unit = Unit.GRAM;
	}
	
	@JsonbProperty
	protected Long getRecipeReference() {
		return this.recipe == null ? null : this.recipe.getIdentity();
	}
	
	@JsonbTransient
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
	
	@JsonbProperty
	public float getAmount () {
		return this.amount;
	}
	
	public void setAmount (final float amount) {
		this.amount = amount;
	}
	
	@JsonbProperty
	public Unit getUnit () {
		return this.unit;
	}
	
	public void setUnit (final Unit unit) {
		this.unit = unit;
	}
}
