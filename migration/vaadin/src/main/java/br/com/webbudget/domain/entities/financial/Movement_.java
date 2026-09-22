package br.com.webbudget.domain.entities.financial;

import br.com.webbudget.domain.entities.registration.Contact;
import java.math.BigDecimal;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Movement.class)
public abstract class Movement_ extends br.com.webbudget.domain.entities.PersistentEntity_ {

	public static volatile SingularAttribute<Movement, String> code;
	public static volatile SingularAttribute<Movement, String> identification;
	public static volatile SetAttribute<Movement, Apportionment> apportionments;
	public static volatile SingularAttribute<Movement, Contact> contact;
	public static volatile SingularAttribute<Movement, String> description;
	public static volatile SingularAttribute<Movement, BigDecimal> value;

	public static final String CODE = "code";
	public static final String IDENTIFICATION = "identification";
	public static final String APPORTIONMENTS = "apportionments";
	public static final String CONTACT = "contact";
	public static final String DESCRIPTION = "description";
	public static final String VALUE = "value";

}

