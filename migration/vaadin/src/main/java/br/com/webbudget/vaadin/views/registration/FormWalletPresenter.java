package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.domain.repositories.registration.WalletRepository;
import br.com.webbudget.domain.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class FormWalletPresenter {
    private final WalletService walletService;
    private final WalletRepository walletRepository;
    public Optional<Wallet> findById(long id) { return walletRepository.findById(id); }
    public void save(Wallet wallet) { walletService.save(wallet); }
    public Wallet update(Wallet wallet) { return walletService.update(wallet); }
}
