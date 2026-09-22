package br.com.webbudget.domain.entities.financial;

import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import java.time.LocalDate;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(PeriodMovement.class)
public abstract class PeriodMovement_ extends br.com.webbudget.domain.entities.financial.Movement_ {

	public static volatile SingularAttribute<PeriodMovement, PeriodMovementType> periodMovementType;
	public static volatile SingularAttribute<PeriodMovement, FinancialPeriod> financialPeriod;
	public static volatile SingularAttribute<PeriodMovement, CreditCardInvoice> creditCardInvoice;
	public static volatile SingularAttribute<PeriodMovement, LocalDate> dueDate;
	public static volatile SingularAttribute<PeriodMovement, Payment> payment;
	public static volatile SingularAttribute<PeriodMovement, PeriodMovementState> periodMovementState;

	public static final String PERIOD_MOVEMENT_TYPE = "periodMovementType";
	public static final String FINANCIAL_PERIOD = "financialPeriod";
	public static final String CREDIT_CARD_INVOICE = "creditCardInvoice";
	public static final String DUE_DATE = "dueDate";
	public static final String PAYMENT = "payment";
	public static final String PERIOD_MOVEMENT_STATE = "periodMovementState";

}

