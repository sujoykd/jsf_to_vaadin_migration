package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.Card;
import br.com.webbudget.domain.repositories.registration.CardRepository;
import br.com.webbudget.domain.services.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class DetailCardPresenter {

    private final CardService cardService;
    private final CardRepository cardRepository;

    public Optional<Card> findById(long id) {
        return cardRepository.findById(id);
    }

    public void delete(Card card) {
        cardService.delete(card);
    }
}
