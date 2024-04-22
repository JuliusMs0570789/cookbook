package edu.sb.cookbook.persistence;

import javax.json.bind.annotation.JsonbProperty;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

// TODO: *:0..1 Relation (beidseitig) zu Person
// TODO: *:1 Relation zu Document
// TODO: *:1 Relation zu Restriction

@Entity
@Table(schema="cookbook", name="IngredientType", indexes={})
@PrimaryKeyJoinColumn(name="ingredientTypeIdentity")
@DiscriminatorValue("IngredientType")

public class IngredientType extends BaseEntity {
	@NotNull @ManyToOne
	public Document avatar;
	
	@ManyToOne @JoinColumn(name = "ingredientTypes")
	public Person owner;
	
	@Column(nullable=true, updatable=true)
	public String alias;
	
	@NotNull @ManyToOne
	@Column(nullable=false, updatable=true) @Enumerated
	public Restriction restriction;
	
	@Column(nullable=true, updatable=true)
	public String description;
	
	
	public IngredientType () {
		this.avatar = null;
		this.owner = null;
		this.alias = null;
		this.restriction = Restriction.values()[0];
		this.description = null;
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
	public String getAlias () {
		return this.alias;
	}
	
	public void setAlias (final String alias) {
		this.alias = alias;
	}
	
	public Restriction getRestriction() {
		return this.restriction;
	}
	
	public void setRestriction(Restriction restriction) {
		this.restriction = restriction;
	}
	
	@JsonbProperty
	public String getDescription () {
		return this.description;
	}
	
	public void setDescription (final String description) {
		this.description = description;
	}
}
