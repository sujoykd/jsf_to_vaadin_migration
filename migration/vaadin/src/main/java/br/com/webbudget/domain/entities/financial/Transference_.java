package br.com.webbudget.domain.entities.financial;

import br.com.webbudget.domain.entities.registration.Wallet;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Transference.class)
public abstract class Transference_ extends br.com.webbudget.domain.entities.PersistentEntity_ {

	public static volatile SingularAttribute<Transference, Wallet> origin;
	public static volatile SingularAttribute<Transference, Wallet> destination;
	public static volatile SingularAttribute<Transference, String> description;
	public static volatile SingularAttribute<Transference, LocalDate> transferDate;
	public static volatile SingularAttribute<Transference, BigDecimal> value;

	public static final String ORIGIN = "origin";
	public static final String DESTINATION = "destination";
	public static final String DESCRIPTION = "description";
	public static final String TRANSFER_DATE = "transferDate";
	public static final String VALUE = "value";

}

