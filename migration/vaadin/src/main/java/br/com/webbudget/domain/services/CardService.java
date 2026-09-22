package br.com.webbudget.domain.services;

import br.com.webbudget.domain.entities.registration.Card;
import br.com.webbudget.domain.events.CardCreatedEvent;
import br.com.webbudget.domain.logics.registration.card.CardDeletingLogic;
import br.com.webbudget.domain.logics.registration.card.CardSavingLogic;
import br.com.webbudget.domain.logics.registration.card.CardUpdatingLogic;
import br.com.webbudget.domain.repositories.registration.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final ApplicationEventPublisher eventPublisher;

    private final List<CardSavingLogic> savingBusinessLogics;
    private final List<CardUpdatingLogic> updatingBusinessLogics;
    private final List<CardDeletingLogic> deletingBusinessLogics;

    @Transactional
    public Card save(Card card) {
        this.savingBusinessLogics.forEach(logic -> logic.run(card));
        final Card saved = this.cardRepository.save(card);
        this.eventPublisher.publishEvent(new CardCreatedEvent(saved));
        return saved;
    }

    @Transactional
    public Card update(Card card) {
        this.updatingBusinessLogics.forEach(logic -> logic.run(card));
        return this.cardRepository.saveAndFlushAndRefresh(card);
    }

    @Transactional
    public void delete(Card card) {
        this.deletingBusinessLogics.forEach(logic -> logic.run(card));
        this.cardRepository.attachAndRemove(card);
    }
}
