package edu.sb.cookbook.persistence;

import java.util.Collections;
import java.util.Set;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.eclipse.persistence.annotations.CacheIndex;
import edu.sb.tool.HashCodes;
import edu.sb.tool.JsonProtectedPropertyStrategy;

@Entity
@Table(schema="cookbook", name="Person", indexes={})
@PrimaryKeyJoinColumn(name="personIdentity")
@DiscriminatorValue("Person")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
public class Person extends BaseEntity {
	static public enum Group {
		USER, ADMIN
	}
		
	static public String DEFAULT_PASSWORD_HASH = HashCodes.sha2HashText(256, "changemeplease");

	@NotNull @Email @Size(max = 128)
	@Column(nullable=false, updatable=true, length = 128, unique = true)
	@CacheIndex(updateable=true)
	private String email;

	@NotNull @NotEmpty @Size(min = 64, max = 64)
	@Column(nullable=false, updatable=true, length = 64)
	private String passwordHash;
	
	@NotNull
	@Enumerated(EnumType.STRING) 
	@Column(nullable=false, updatable=true, name = "groupAlias")
	private Group group;
	
	@NotNull @Valid
	@Embedded
	private Name name;

	@NotNull @Valid
	@Embedded
	private Address address;

	@NotNull
	@ElementCollection
	@CollectionTable(
		schema = "cookbook",
		name = "PhoneAssociation",
		joinColumns = @JoinColumn(nullable=false, updatable=false, insertable=true, name = "personReference"),
		uniqueConstraints = @UniqueConstraint(columnNames = {"personReference", "phone"})
	)
	@Column(nullable=false, updatable=false,  insertable=true, name = "phone") // Ausnahme auf Grund von CollectionTable
	private Set<String> phones;
	
	@ManyToOne(optional = false)
	@JoinColumn(nullable=false, updatable=true, name = "avatarReference")
	private Document avatar;

	@NotNull
	@OneToMany(mappedBy = "owner", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH}) // kein CascadeType.REMOVE wegen ON DELETE SET NULL
	private Set<Recipe> recipes;

	@NotNull
	@OneToMany(mappedBy = "owner", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH}) // kein CascadeType.REMOVE wegen ON DELETE SET NULL
	private Set<IngredientType> ingredientTypes; 
	
	public Person() {
		super();
		this.passwordHash = DEFAULT_PASSWORD_HASH;
		this.group = Group.USER;
		this.name = new Name();
		this.address = new Address();
		this.phones = Collections.emptySet();
		this.recipes = Collections.emptySet();
		this.ingredientTypes = Collections.emptySet();
	}
	
	@JsonbProperty
	public Document getAvatar () {
		return this.avatar;
	}
	
	public void setAvatar (final Document avatar) {
		this.avatar = avatar;
	}
	
	@JsonbTransient
	public Set<Recipe> getRecipes () {
		return this.recipes;
	}
	
	protected void setRecipes (final Set<Recipe> recipes) {
		this.recipes = recipes;
	}
	
	@JsonbTransient
	public Set<IngredientType> getIngredientTypes () {
		return this.ingredientTypes;
	}
	
	protected void setIngredientTypes (final Set<IngredientType> ingredientTypes) {
		this.ingredientTypes = ingredientTypes;
	}
	
	@JsonbProperty
	public String getEmail () {
		return this.email;
	}
	
	public void setEmail (final String email) {
		this.email = email;
	}
	
	@JsonbTransient
	public String getPasswordHash () {
		return this.passwordHash;
	}
	
	public void setPasswordHash (final String passwordHash) {
		this.passwordHash = passwordHash;
	}
	
	@JsonbProperty
	public Person.Group getGroup() {
		return this.group;
	}
	
	public void setGroup(Person.Group group) {
		this.group = group;
	}
	
	@JsonbProperty
	public Name getName () {
		return this.name;
	}
	
	protected void setName (final Name name) {
		this.name = name;
	}
	
	@JsonbProperty
	public Address getAddress () {
		return this.address;
	}
	
	protected void setAddress (final Address address) {
		this.address = address;
	}
	
	@JsonbProperty
	public Set<String> getPhones () {
		return this.phones;
	}
	
	protected void setPhones (final Set<String> phones) {
		this.phones = phones;
	}
}
