package br.com.webbudget.application.components.ui.filter;

import br.com.webbudget.domain.entities.financial.BalanceType;
import br.com.webbudget.domain.entities.financial.ReasonType;
import br.com.webbudget.domain.entities.registration.Wallet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class WalletBalanceFilter extends BasicFilter {

    @Getter
    @Setter
    private ReasonType reasonType;
    @Getter
    @Setter
    private BalanceType balanceType;

    @Getter
    @Setter
    private LocalDate operationDate;

    @Getter
    @Setter
    private Wallet wallet;

    public WalletBalanceFilter(Wallet wallet) {
        this.wallet = wallet;
    }

    public ReasonType[] getReasonTypes() {
        return ReasonType.values();
    }

    public BalanceType[] getBalanceTypes() {
        return BalanceType.values();
    }

    public void clear() {
        this.reasonType = null;
        this.balanceType = null;
        this.operationDate = null;
    }
}
