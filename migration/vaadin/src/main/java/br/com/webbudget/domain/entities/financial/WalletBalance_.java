package br.com.webbudget.domain.entities.financial;

import br.com.webbudget.domain.entities.registration.Wallet;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(WalletBalance.class)
public abstract class WalletBalance_ extends br.com.webbudget.domain.entities.PersistentEntity_ {

	public static volatile SingularAttribute<WalletBalance, BalanceType> balanceType;
	public static volatile SingularAttribute<WalletBalance, Wallet> wallet;
	public static volatile SingularAttribute<WalletBalance, LocalDateTime> movementDateTime;
	public static volatile SingularAttribute<WalletBalance, BigDecimal> actualBalance;
	public static volatile SingularAttribute<WalletBalance, String> observations;
	public static volatile SingularAttribute<WalletBalance, BigDecimal> oldBalance;
	public static volatile SingularAttribute<WalletBalance, String> movementCode;
	public static volatile SingularAttribute<WalletBalance, BigDecimal> transactionValue;
	public static volatile SingularAttribute<WalletBalance, ReasonType> reasonType;

	public static final String BALANCE_TYPE = "balanceType";
	public static final String WALLET = "wallet";
	public static final String MOVEMENT_DATE_TIME = "movementDateTime";
	public static final String ACTUAL_BALANCE = "actualBalance";
	public static final String OBSERVATIONS = "observations";
	public static final String OLD_BALANCE = "oldBalance";
	public static final String MOVEMENT_CODE = "movementCode";
	public static final String TRANSACTION_VALUE = "transactionValue";
	public static final String REASON_TYPE = "reasonType";

}

