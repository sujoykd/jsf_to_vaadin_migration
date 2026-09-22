package br.com.webbudget.domain.entities.registration;

import java.math.BigDecimal;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Wallet.class)
public abstract class Wallet_ extends br.com.webbudget.domain.entities.PersistentEntity_ {

	public static volatile SingularAttribute<Wallet, String> bank;
	public static volatile SingularAttribute<Wallet, String> agency;
	public static volatile SingularAttribute<Wallet, BigDecimal> actualBalance;
	public static volatile SingularAttribute<Wallet, WalletType> walletType;
	public static volatile SingularAttribute<Wallet, String> name;
	public static volatile SingularAttribute<Wallet, String> description;
	public static volatile SingularAttribute<Wallet, Boolean> active;
	public static volatile SingularAttribute<Wallet, String> account;
	public static volatile SingularAttribute<Wallet, String> digit;

	public static final String BANK = "bank";
	public static final String AGENCY = "agency";
	public static final String ACTUAL_BALANCE = "actualBalance";
	public static final String WALLET_TYPE = "walletType";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String ACTIVE = "active";
	public static final String ACCOUNT = "account";
	public static final String DIGIT = "digit";

}

