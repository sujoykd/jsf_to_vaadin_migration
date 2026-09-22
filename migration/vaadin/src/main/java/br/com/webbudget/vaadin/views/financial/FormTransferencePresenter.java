package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.Transference;
import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.domain.repositories.registration.WalletRepository;
import br.com.webbudget.domain.services.TransferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class FormTransferencePresenter {

    private final TransferenceService transferenceService;
    private final WalletRepository walletRepository;

    public List<Wallet> loadWallets() {
        return walletRepository.findAllActive();
    }

    public void transfer(Transference transference) {
        transferenceService.transfer(transference);
    }
}
