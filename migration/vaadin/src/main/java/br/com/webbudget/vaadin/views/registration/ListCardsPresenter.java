package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.registration.Card;
import br.com.webbudget.domain.repositories.registration.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ListCardsPresenter {

    private final CardRepository cardRepository;

    public Page<Card> findAll(String filter, Boolean active, int offset, int limit) {
        return cardRepository.findAllBy(filter, active, offset, limit);
    }

    public int count(String filter, Boolean active) {
        return (int) cardRepository.count(cardRepository.buildSpecification(filter, active));
    }
}
