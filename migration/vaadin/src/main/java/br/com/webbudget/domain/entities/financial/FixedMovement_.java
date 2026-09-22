package br.com.webbudget.domain.entities.financial;

import java.time.LocalDate;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(FixedMovement.class)
public abstract class FixedMovement_ extends br.com.webbudget.domain.entities.financial.Movement_ {

	public static volatile SingularAttribute<FixedMovement, Integer> totalQuotes;
	public static volatile SingularAttribute<FixedMovement, Integer> actualQuote;
	public static volatile SingularAttribute<FixedMovement, Boolean> autoLaunch;
	public static volatile SingularAttribute<FixedMovement, Boolean> undetermined;
	public static volatile SingularAttribute<FixedMovement, FixedMovementState> fixedMovementState;
	public static volatile SingularAttribute<FixedMovement, Integer> startingQuote;
	public static volatile SingularAttribute<FixedMovement, LocalDate> startDate;

	public static final String TOTAL_QUOTES = "totalQuotes";
	public static final String ACTUAL_QUOTE = "actualQuote";
	public static final String AUTO_LAUNCH = "autoLaunch";
	public static final String UNDETERMINED = "undetermined";
	public static final String FIXED_MOVEMENT_STATE = "fixedMovementState";
	public static final String STARTING_QUOTE = "startingQuote";
	public static final String START_DATE = "startDate";

}

