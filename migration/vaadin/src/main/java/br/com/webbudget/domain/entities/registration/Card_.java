package br.com.webbudget.domain.entities.registration;

import java.math.BigDecimal;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Card.class)
public abstract class Card_ extends br.com.webbudget.domain.entities.PersistentEntity_ {

	public static volatile SingularAttribute<Card, String> owner;
	public static volatile SingularAttribute<Card, String> number;
	public static volatile SingularAttribute<Card, Integer> expirationDay;
	public static volatile SingularAttribute<Card, String> flag;
	public static volatile SingularAttribute<Card, Wallet> wallet;
	public static volatile SingularAttribute<Card, String> name;
	public static volatile SingularAttribute<Card, CardType> cardType;
	public static volatile SingularAttribute<Card, BigDecimal> creditLimit;
	public static volatile SingularAttribute<Card, Boolean> active;

	public static final String OWNER = "owner";
	public static final String NUMBER = "number";
	public static final String EXPIRATION_DAY = "expirationDay";
	public static final String FLAG = "flag";
	public static final String WALLET = "wallet";
	public static final String NAME = "name";
	public static final String CARD_TYPE = "cardType";
	public static final String CREDIT_LIMIT = "creditLimit";
	public static final String ACTIVE = "active";

}

