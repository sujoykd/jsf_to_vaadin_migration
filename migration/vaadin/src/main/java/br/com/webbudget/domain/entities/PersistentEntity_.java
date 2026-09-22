package br.com.webbudget.domain.entities;

import java.time.LocalDateTime;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(PersistentEntity.class)
public abstract class PersistentEntity_ {

	public static volatile SingularAttribute<PersistentEntity, Long> id;
	public static volatile SingularAttribute<PersistentEntity, LocalDateTime> updatedOn;
	public static volatile SingularAttribute<PersistentEntity, LocalDateTime> createdOn;

	public static final String ID = "id";
	public static final String UPDATED_ON = "updatedOn";
	public static final String CREATED_ON = "createdOn";

}

