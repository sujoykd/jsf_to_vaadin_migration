package br.com.webbudget.domain.services;

import br.com.webbudget.domain.entities.registration.Card;
import br.com.webbudget.domain.entities.registration.CardType;
import br.com.webbudget.domain.events.CardCreatedEvent;
import br.com.webbudget.domain.logics.registration.card.CardDeletingLogic;
import br.com.webbudget.domain.logics.registration.card.CardSavingLogic;
import br.com.webbudget.domain.logics.registration.card.CardUpdatingLogic;
import br.com.webbudget.domain.repositories.registration.CardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CardServiceTest {

    @Test
    void save_runs_logics_persists_and_fires_event() {
        var card = card("Visa");
        var repo = mock(CardRepository.class);
        when(repo.save(card)).thenReturn(card);
        var publisher = mock(ApplicationEventPublisher.class);
        var savingLogic = mock(CardSavingLogic.class);

        var service = new CardService(repo, publisher, List.of(savingLogic), List.of(), List.of());
        var result = service.save(card);

        verify(savingLogic).run(card);
        verify(repo).save(card);
        verify(publisher).publishEvent(new CardCreatedEvent(card));
        assertThat(result).isSameAs(card);
    }

    @Test
    void update_runs_logics_and_refreshes() {
        var card = card("Master");
        var repo = mock(CardRepository.class);
        when(repo.saveAndFlushAndRefresh(card)).thenReturn(card);
        var publisher = mock(ApplicationEventPublisher.class);
        var updatingLogic = mock(CardUpdatingLogic.class);

        var service = new CardService(repo, publisher, List.of(), List.of(updatingLogic), List.of());
        var result = service.update(card);

        verify(updatingLogic).run(card);
        verify(repo).saveAndFlushAndRefresh(card);
        assertThat(result).isSameAs(card);
    }

    @Test
    void delete_runs_logics_and_removes() {
        var card = card("Elo");
        var repo = mock(CardRepository.class);
        var publisher = mock(ApplicationEventPublisher.class);
        var deletingLogic = mock(CardDeletingLogic.class);

        var service = new CardService(repo, publisher, List.of(), List.of(), List.of(deletingLogic));
        service.delete(card);

        verify(deletingLogic).run(card);
        verify(repo).attachAndRemove(card);
    }

    @Test
    void save_without_logics_still_persists_and_fires_event() {
        var card = card("Amex");
        var repo = mock(CardRepository.class);
        when(repo.save(card)).thenReturn(card);
        var publisher = mock(ApplicationEventPublisher.class);

        var service = new CardService(repo, publisher, List.of(), List.of(), List.of());
        service.save(card);

        verify(repo).save(card);
        verify(publisher).publishEvent(new CardCreatedEvent(card));
    }

    private Card card(String name) {
        var c = new Card();
        c.setName(name);
        c.setCardType(CardType.CREDIT);
        return c;
    }
}
