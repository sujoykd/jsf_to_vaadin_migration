package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.application.components.ui.filter.TransferenceFilter;
import br.com.webbudget.domain.entities.financial.Transference;
import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.domain.repositories.financial.TransferenceRepository;
import br.com.webbudget.domain.repositories.registration.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class TransferenceHistoricPresenter {

    private final TransferenceRepository transferencRepository;
    private final WalletRepository walletRepository;

    public List<Wallet> loadWallets() {
        return walletRepository.findAll();
    }

    public List<Transference> filter(TransferenceFilter filter) {
        return transferencRepository.findByFilter(filter);
    }
}
