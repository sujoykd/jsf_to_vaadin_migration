package br.com.webbudget.domain.entities.financial;

import br.com.webbudget.domain.entities.registration.Card;
import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(CreditCardInvoice.class)
public abstract class CreditCardInvoice_ extends br.com.webbudget.domain.entities.PersistentEntity_ {

	public static volatile SingularAttribute<CreditCardInvoice, BigDecimal> totalValue;
	public static volatile SingularAttribute<CreditCardInvoice, FinancialPeriod> financialPeriod;
	public static volatile SingularAttribute<CreditCardInvoice, PeriodMovement> periodMovement;
	public static volatile SingularAttribute<CreditCardInvoice, String> identification;
	public static volatile SingularAttribute<CreditCardInvoice, LocalDate> dueDate;
	public static volatile SingularAttribute<CreditCardInvoice, InvoiceState> invoiceState;
	public static volatile SingularAttribute<CreditCardInvoice, LocalDate> closingDate;
	public static volatile SingularAttribute<CreditCardInvoice, LocalDate> paymentDate;
	public static volatile ListAttribute<CreditCardInvoice, PeriodMovement> periodMovements;
	public static volatile SingularAttribute<CreditCardInvoice, Card> card;

	public static final String TOTAL_VALUE = "totalValue";
	public static final String FINANCIAL_PERIOD = "financialPeriod";
	public static final String PERIOD_MOVEMENT = "periodMovement";
	public static final String IDENTIFICATION = "identification";
	public static final String DUE_DATE = "dueDate";
	public static final String INVOICE_STATE = "invoiceState";
	public static final String CLOSING_DATE = "closingDate";
	public static final String PAYMENT_DATE = "paymentDate";
	public static final String PERIOD_MOVEMENTS = "periodMovements";
	public static final String CARD = "card";

}

