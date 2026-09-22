package br.com.webbudget.domain.entities.registration;

import java.math.BigDecimal;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(CostCenter.class)
public abstract class CostCenter_ extends br.com.webbudget.domain.entities.PersistentEntity_ {

	public static volatile SingularAttribute<CostCenter, CostCenter> parent;
	public static volatile SingularAttribute<CostCenter, BigDecimal> revenuesBudget;
	public static volatile SingularAttribute<CostCenter, String> name;
	public static volatile SingularAttribute<CostCenter, String> description;
	public static volatile SingularAttribute<CostCenter, Boolean> active;
	public static volatile SingularAttribute<CostCenter, BigDecimal> expensesBudget;

	public static final String PARENT = "parent";
	public static final String REVENUES_BUDGET = "revenuesBudget";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String ACTIVE = "active";
	public static final String EXPENSES_BUDGET = "expensesBudget";

}

