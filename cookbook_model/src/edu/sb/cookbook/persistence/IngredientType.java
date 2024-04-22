package edu.sb.cookbook.persistence;

import javax.json.bind.annotation.JsonbProperty;

// TODO: *:0..1 Relation (beidseitig) zu Person
// TODO: *:1 Relation zu Document
// TODO: *:1 Relation zu Restriction

public class IngredientType extends BaseEntity {
	public Document avatar;
	
	public Person owner;
	
	public String alias;
	
	public Restriction restriction;
	
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
