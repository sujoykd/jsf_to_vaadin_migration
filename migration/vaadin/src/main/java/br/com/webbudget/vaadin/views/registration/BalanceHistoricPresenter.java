package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.financial.WalletBalance;
import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.domain.repositories.registration.WalletBalanceRepository;
import br.com.webbudget.domain.repositories.registration.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class BalanceHistoricPresenter {
    private final WalletRepository walletRepository;
    private final WalletBalanceRepository walletBalanceRepository;
    public Optional<Wallet> findById(long id) { return walletRepository.findById(id); }
    public List<WalletBalance> findHistoricByWalletId(long walletId) { return walletBalanceRepository.findByWallet_id(walletId); }
}
