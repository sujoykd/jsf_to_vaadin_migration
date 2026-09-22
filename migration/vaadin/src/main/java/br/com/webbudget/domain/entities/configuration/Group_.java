package br.com.webbudget.domain.entities.configuration;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Group.class)
public abstract class Group_ extends br.com.webbudget.domain.entities.PersistentEntity_ {

	public static volatile ListAttribute<Group, Grant> grants;
	public static volatile SingularAttribute<Group, Group> parent;
	public static volatile SingularAttribute<Group, String> name;
	public static volatile SingularAttribute<Group, Boolean> active;

	public static final String GRANTS = "grants";
	public static final String PARENT = "parent";
	public static final String NAME = "name";
	public static final String ACTIVE = "active";

}

