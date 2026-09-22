package br.com.webbudget.application.components.builder;

import br.com.webbudget.domain.entities.financial.BalanceType;
import br.com.webbudget.domain.entities.financial.ReasonType;
import br.com.webbudget.domain.entities.financial.WalletBalance;
import br.com.webbudget.domain.entities.registration.Wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class WalletBalanceBuilder extends AbstractBuilder<WalletBalance> {

    private WalletBalanceBuilder() {
        this.instance = new WalletBalance();
        this.instance.setMovementDateTime(LocalDateTime.now());
    }

    public static WalletBalanceBuilder getInstance() {
        return new WalletBalanceBuilder();
    }

    public WalletBalanceBuilder to(Wallet target) {
        this.instance.setWallet(target);
        return this;
    }

    public WalletBalanceBuilder value(BigDecimal value) {
        if (value.signum() < 0) {
            this.instance.setBalanceType(BalanceType.DEBIT);
        } else {
            this.instance.setBalanceType(BalanceType.CREDIT);
        }
        this.instance.setTransactionValue(value);
        return this;
    }

    public WalletBalanceBuilder withObservations(String observations) {
        this.instance.setObservations(observations);
        return this;
    }

    public WalletBalanceBuilder forMovement(String movementCode) {
        this.instance.setMovementCode(movementCode);
        return this;
    }

    public WalletBalanceBuilder withMovementDate(LocalDateTime movementDate) {
        this.instance.setMovementDateTime(movementDate);
        return this;
    }

    public WalletBalanceBuilder withReason(ReasonType reason) {
        this.instance.setReasonType(reason);
        return this;
    }

    @Override
    public WalletBalance build() {
        this.instance.processBalances();
        return this.instance;
    }
}
