package br.com.webbudget.domain.entities.registration;

import br.com.webbudget.domain.entities.financial.Closing;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(FinancialPeriod.class)
public abstract class FinancialPeriod_ extends br.com.webbudget.domain.entities.PersistentEntity_ {

	public static volatile SingularAttribute<FinancialPeriod, BigDecimal> revenuesGoal;
	public static volatile SingularAttribute<FinancialPeriod, String> identification;
	public static volatile SingularAttribute<FinancialPeriod, Boolean> expired;
	public static volatile SingularAttribute<FinancialPeriod, Closing> closing;
	public static volatile SingularAttribute<FinancialPeriod, BigDecimal> expensesGoal;
	public static volatile SingularAttribute<FinancialPeriod, LocalDate> start;
	public static volatile SingularAttribute<FinancialPeriod, Boolean> closed;
	public static volatile SingularAttribute<FinancialPeriod, LocalDate> end;
	public static volatile SingularAttribute<FinancialPeriod, BigDecimal> creditCardGoal;

	public static final String REVENUES_GOAL = "revenuesGoal";
	public static final String IDENTIFICATION = "identification";
	public static final String EXPIRED = "expired";
	public static final String CLOSING = "closing";
	public static final String EXPENSES_GOAL = "expensesGoal";
	public static final String START = "start";
	public static final String CLOSED = "closed";
	public static final String END = "end";
	public static final String CREDIT_CARD_GOAL = "creditCardGoal";

}

