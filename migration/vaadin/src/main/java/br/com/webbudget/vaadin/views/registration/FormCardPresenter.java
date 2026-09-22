package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.Card;
import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.domain.repositories.registration.CardRepository;
import br.com.webbudget.domain.repositories.registration.WalletRepository;
import br.com.webbudget.domain.services.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class FormCardPresenter {

    private final CardService cardService;
    private final CardRepository cardRepository;
    private final WalletRepository walletRepository;

    public Optional<Card> findById(long id) {
        return cardRepository.findById(id);
    }

    public Card save(Card card) {
        return cardService.save(card);
    }

    public Card update(Card card) {
        return cardService.update(card);
    }

    public List<Wallet> findActiveWallets() {
        return walletRepository.findAllActive();
    }
}
