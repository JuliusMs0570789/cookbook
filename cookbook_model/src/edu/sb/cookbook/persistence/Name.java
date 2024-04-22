package edu.sb.cookbook.persistence;

import javax.json.bind.annotation.JsonbProperty;
import javax.validation.constraints.Size;


public class Name implements Comparable<Name> {
	@Size(max=10)
	private String title;
	
	@Size(max=40)
	private String family;
	
	@Size(max=40)
	private String given;
	
	/**
	 * Initializes a new instance.
	 */
	public Name () {
		this.title = null;
		this.family = null;
		this.given = null;
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
	 * Returns the family.
	 * @return the family, or {@code null} for none
	 */
	@JsonbProperty
	public String getFamily () {
		return this.family;
	}
	
	/**
	 * Sets the family.
	 * @param family the family, or {@code null} for none
	 */
	public void setFamily (final String family) {
		this.family = family;
	}
	
	/**
	 * Returns the given.
	 * @return the given, or {@code null} for none
	 */
	@JsonbProperty
	public String getGiven () {
		return this.given;
	}
	
	/**
	 * Sets the given.
	 * @param given the given, or {@code null} for none
	 */
	public void setGiven (final String given) {
		this.given = given;
	}

	@Override
	public int compareTo(Name o) {
		// TODO Auto-generated method stub
		return 0;
	}
}
