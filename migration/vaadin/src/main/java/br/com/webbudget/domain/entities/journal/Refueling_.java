package br.com.webbudget.domain.entities.journal;

import br.com.webbudget.domain.entities.financial.PeriodMovement;
import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.entities.registration.MovementClass;
import br.com.webbudget.domain.entities.registration.Vehicle;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Refueling.class)
public abstract class Refueling_ extends br.com.webbudget.domain.entities.PersistentEntity_ {

	public static volatile SingularAttribute<Refueling, String> code;
	public static volatile SingularAttribute<Refueling, BigDecimal> cost;
	public static volatile SingularAttribute<Refueling, Long> odometer;
	public static volatile SingularAttribute<Refueling, Long> distance;
	public static volatile SingularAttribute<Refueling, BigDecimal> liters;
	public static volatile ListAttribute<Refueling, Fuel> fuels;
	public static volatile SingularAttribute<Refueling, String> accountedBy;
	public static volatile SingularAttribute<Refueling, Boolean> firstRefueling;
	public static volatile SingularAttribute<Refueling, Boolean> fullTank;
	public static volatile SingularAttribute<Refueling, BigDecimal> costPerLiter;
	public static volatile SingularAttribute<Refueling, Vehicle> vehicle;
	public static volatile SingularAttribute<Refueling, PeriodMovement> periodMovement;
	public static volatile SingularAttribute<Refueling, FinancialPeriod> financialPeriod;
	public static volatile SingularAttribute<Refueling, Boolean> accounted;
	public static volatile SingularAttribute<Refueling, BigDecimal> averageConsumption;
	public static volatile SingularAttribute<Refueling, MovementClass> movementClass;
	public static volatile SingularAttribute<Refueling, String> place;
	public static volatile SingularAttribute<Refueling, LocalDate> eventDate;

	public static final String CODE = "code";
	public static final String COST = "cost";
	public static final String ODOMETER = "odometer";
	public static final String DISTANCE = "distance";
	public static final String LITERS = "liters";
	public static final String FUELS = "fuels";
	public static final String ACCOUNTED_BY = "accountedBy";
	public static final String FIRST_REFUELING = "firstRefueling";
	public static final String FULL_TANK = "fullTank";
	public static final String COST_PER_LITER = "costPerLiter";
	public static final String VEHICLE = "vehicle";
	public static final String PERIOD_MOVEMENT = "periodMovement";
	public static final String FINANCIAL_PERIOD = "financialPeriod";
	public static final String ACCOUNTED = "accounted";
	public static final String AVERAGE_CONSUMPTION = "averageConsumption";
	public static final String MOVEMENT_CLASS = "movementClass";
	public static final String PLACE = "place";
	public static final String EVENT_DATE = "eventDate";

}

