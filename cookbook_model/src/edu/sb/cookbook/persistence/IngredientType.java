package edu.sb.cookbook.persistence;

import javax.json.bind.annotation.JsonbProperty;

public class IngredientType {
	public Document avatar;
	
	public Person owner;
	
	public String alias;
	
	public IngredientType.restrictions restriction;
	
	public String description;
	
	static public enum restrictions {
		NONE, PESCATARIAN, LACTO_OVO_VEGETARIAN, LACTO_VEGETARIAN, VEGAN	
	}
	
	public IngredientType () {
		this.avatar = null;
		this.owner = null;
		this.alias = null;
		this.restriction = restrictions.values()[0];
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
	
	public IngredientType.restrictions getRestriction() {
		return this.restriction;
	}
	
	public void setRestriction(IngredientType.restrictions restriction) {
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
