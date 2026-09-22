package br.com.webbudget.domain.entities.financial;

import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.domain.entities.registration.MovementClass;
import java.math.BigDecimal;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Apportionment.class)
public abstract class Apportionment_ extends br.com.webbudget.domain.entities.PersistentEntity_ {

	public static volatile SingularAttribute<Apportionment, String> code;
	public static volatile SingularAttribute<Apportionment, CostCenter> costCenter;
	public static volatile SingularAttribute<Apportionment, MovementClass> movementClass;
	public static volatile SingularAttribute<Apportionment, BigDecimal> value;
	public static volatile SingularAttribute<Apportionment, Movement> movement;

	public static final String CODE = "code";
	public static final String COST_CENTER = "costCenter";
	public static final String MOVEMENT_CLASS = "movementClass";
	public static final String VALUE = "value";
	public static final String MOVEMENT = "movement";

}

