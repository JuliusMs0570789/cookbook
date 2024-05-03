package edu.sb.cookbook.persistence;

import javax.json.bind.annotation.JsonbProperty;

public class IngredientType {
	public Document avatar;
	
	public Person owner;
	
	public String alias;
	
	static public enum restriction {
		NONE, PESCATARIAN, LACTO_OVO_VEGETARIAN, LACTO_VEGETARIAN, VEGAN	
	}
	
	public String description;
	
	
	public IngredientType () {
		this.avatar = null;
		this.owner = null;
		this.alias = null;
		// TODO: handle restricition
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
	
	// TODO: add methods getRestriction and setRestriction
	
	@JsonbProperty
	public String getDescription () {
		return this.description;
	}
	
	public void setDescription (final String description) {
		this.description = description;
	}
}
