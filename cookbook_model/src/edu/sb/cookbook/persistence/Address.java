package edu.sb.cookbook.persistence;

import javax.json.bind.annotation.JsonbProperty;

import javax.validation.constraints.Size;

public class Address {
	@Size(max=15)
	private String postCode;
	
	@Size(max=50)
	private String street;
	
	@Size(max=50)
	private String city;
	
	@Size(max=50)
	private String country;
	
	/**
	 * Initializes a new instance.
	 */
	public Address () {
		this.postCode = null;
		this.street = null;
		this.city = null;
		this.country = null;
	}
	
	/**
	 * Returns the postCode.
	 * @return the postCode, or {@code null} for none
	 */
	@JsonbProperty
	public String getPostcode () {
		return this.postCode;
	}
	
	/**
	 * Sets the postCode.
	 * @param postCode the postCode, or {@code null} for none
	 */
	public void setPostcode (final String postCode) {
		this.postCode = postCode;
	}
	
	/**
	 * Returns the street.
	 * @return the street, or {@code null} for none
	 */
	@JsonbProperty
	public String getStreet () {
		return this.street;
	}
	
	/**
	 * Sets the Street.
	 * @param street the street, or {@code null} for none
	 */
	public void setStreet (final String street) {
		this.street = street;
	}
	
	/**
	 * Returns the city.
	 * @return the city, or {@code null} for none
	 */
	@JsonbProperty
	public String getCity () {
		return this.city;
	}
	
	/**
	 * Sets the City.
	 * @param city the city, or {@code null} for none
	 */
	public void setCity (final String city) {
		this.city = city;
	}
	
	/**
	 * Returns the country.
	 * @return the country, or {@code null} for none
	 */
	@JsonbProperty
	public String getCountry () {
		return this.country;
	}
	
	/**
	 * Sets the Country.
	 * @param country the country, or {@code null} for none
	 */
	public void setCountry (final String country) {
		this.country = country;
	}
}
