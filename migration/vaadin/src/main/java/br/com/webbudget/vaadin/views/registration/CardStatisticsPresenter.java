package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.financial.CreditCardInvoice;
import br.com.webbudget.domain.entities.registration.Card;
import br.com.webbudget.domain.entities.view.CardConsume;
import br.com.webbudget.domain.entities.view.CardConsumeDetailed;
import br.com.webbudget.domain.repositories.financial.CreditCardInvoiceRepository;
import br.com.webbudget.domain.repositories.registration.CardRepository;
import br.com.webbudget.domain.repositories.view.CardConsumeDetailedRepository;
import br.com.webbudget.domain.repositories.view.CardConsumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class CardStatisticsPresenter {

    private final CardRepository cardRepository;
    private final CreditCardInvoiceRepository creditCardInvoiceRepository;
    private final CardConsumeRepository cardConsumeRepository;
    private final CardConsumeDetailedRepository cardConsumeDetailedRepository;

    public Optional<Card> findById(long id) {
        return cardRepository.findById(id);
    }

    public List<CreditCardInvoice> findInvoicesByCard(Card card) {
        return creditCardInvoiceRepository.findByCard(card);
    }

    public List<CardConsume> findConsumeByCardId(long cardId) {
        return cardConsumeRepository.findByCardId(cardId);
    }

    public List<CardConsumeDetailed> findDetailedConsumeByCardId(long cardId) {
        return cardConsumeDetailedRepository.findByCardId(cardId);
    }
}
