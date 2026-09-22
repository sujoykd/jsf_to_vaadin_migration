package br.com.webbudget.domain.entities.registration;

import java.math.BigDecimal;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(MovementClass.class)
public abstract class MovementClass_ extends br.com.webbudget.domain.entities.PersistentEntity_ {

	public static volatile SingularAttribute<MovementClass, MovementClassType> movementClassType;
	public static volatile SingularAttribute<MovementClass, CostCenter> costCenter;
	public static volatile SingularAttribute<MovementClass, String> name;
	public static volatile SingularAttribute<MovementClass, Boolean> active;
	public static volatile SingularAttribute<MovementClass, BigDecimal> budget;

	public static final String MOVEMENT_CLASS_TYPE = "movementClassType";
	public static final String COST_CENTER = "costCenter";
	public static final String NAME = "name";
	public static final String ACTIVE = "active";
	public static final String BUDGET = "budget";

}

