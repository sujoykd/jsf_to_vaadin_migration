package br.com.webbudget.domain.entities.configuration;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(User.class)
public abstract class User_ extends br.com.webbudget.domain.entities.PersistentEntity_ {

	public static volatile SingularAttribute<User, String> password;
	public static volatile SingularAttribute<User, StoreType> storeType;
	public static volatile SingularAttribute<User, Profile> profile;
	public static volatile SingularAttribute<User, String> name;
	public static volatile SingularAttribute<User, Boolean> active;
	public static volatile SingularAttribute<User, String> email;
	public static volatile SingularAttribute<User, String> username;
	public static volatile SingularAttribute<User, Group> group;

	public static final String PASSWORD = "password";
	public static final String STORE_TYPE = "storeType";
	public static final String PROFILE = "profile";
	public static final String NAME = "name";
	public static final String ACTIVE = "active";
	public static final String EMAIL = "email";
	public static final String USERNAME = "username";
	public static final String GROUP = "group";

}

