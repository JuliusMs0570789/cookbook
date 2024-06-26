package edu.sb.cookbook.persistence;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.persistence.annotations.CacheIndex;

import edu.sb.tool.JsonProtectedPropertyStrategy;

@Entity
@Table(schema="cookbook", name="Recipe", indexes={})
@PrimaryKeyJoinColumn(name="recipeIdentity")
@DiscriminatorValue("Recipe")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
public class Recipe extends BaseEntity {
	static public enum Category {
		MAIN_COURSE, APPETIZER, SNACK, DESSERT, BREAKFAST, BUFFET, BARBEQUE, ADOLESCENT, INFANT		
	}
	
	@NotNull @Size(max = 128)
	@Column(nullable=false, updatable=true, unique = true, length = 128)
	@CacheIndex(updateable=true)
	private String title;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable=false, updatable=true)
	private Category category;
	
	@Size(max = 4094)
	@Column(nullable=true, updatable=true, length = 4094)
	private String description;
	
	@Size(max = 4094)
	@Column(nullable=true, updatable=true, length = 4094)
	private String instruction;
	
	@ManyToOne(optional = false)
	@JoinColumn(nullable=false, updatable=true, name = "avatarReference")
	private Document avatar;
	
	@ManyToOne(optional = true)
	@JoinColumn(nullable=true, updatable=true, name = "ownerReference")
	private Person owner;
	
	@NotNull
	@OneToMany(mappedBy = "recipe", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	private Set<Ingredient> ingredients;
	
	@NotNull
	@ManyToMany
	@JoinTable(
		schema = "cookbook",
		name = "RecipeIllustrationAssociation",
		joinColumns = @JoinColumn(nullable=false, updatable=false, insertable=true, name = "recipeReference"),
		inverseJoinColumns = @JoinColumn(nullable=false, updatable=false, insertable=true, name = "documentReference"),
		uniqueConstraints = @UniqueConstraint(columnNames = {"recipeReference", "documentReference"})
	)
	private Set<Document> illustrations;
	
	/**
	 * Initializes a new instance.
	 * @param content the content, or {@code null} for none
	 */
	public Recipe () {
		super();
		this.category = Category.MAIN_COURSE;
		this.ingredients = Collections.emptySet();
		this.illustrations = new HashSet<>();
	}
	
	
	@JsonbTransient
	public Document getAvatar () {
		return this.avatar;
	}
	
	public void setAvatar (final Document avatar) {
		this.avatar = avatar;
	}
	
	@JsonbTransient
	public Person getOwner () {
		return this.owner;
	}
	
	public void setOwner (final Person owner) {
		this.owner = owner;
	}
	
	@JsonbTransient
	public Set<Ingredient> getIngredients () {
		return this.ingredients;
	}
	
	protected void setIngredients (final Set<Ingredient> ingredients) {
		this.ingredients = ingredients;
	}
	
	@JsonbTransient
	public Set<Document> getIllustrations () {
		return this.illustrations;
	}
	
	protected void setIllustrations (final Set<Document> illustrations) {
		this.illustrations = illustrations;
	}
	
	@JsonbProperty
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
	
	@JsonbProperty
	protected Long getOwnerReference(){
		return this.owner == null ? null : this.owner.getIdentity();
	}
	
	@JsonbProperty
	protected int getIngredientCount(){
		return ingredients.toArray().length;
	}
	
	@JsonbProperty
	public Restriction getRestriction() {
		return this.ingredients.stream().map(Ingredient::getType).map(IngredientType::getRestriction).min(Comparator.naturalOrder()).orElse(Restriction.VEGAN);
    }
}
