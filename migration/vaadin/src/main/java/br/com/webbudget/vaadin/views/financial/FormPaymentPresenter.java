package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.Payment;
import br.com.webbudget.domain.entities.financial.PeriodMovement;
import br.com.webbudget.domain.entities.registration.Card;
import br.com.webbudget.domain.entities.registration.CardType;
import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.domain.repositories.financial.PeriodMovementRepository;
import br.com.webbudget.domain.repositories.registration.CardRepository;
import br.com.webbudget.domain.repositories.registration.WalletRepository;
import br.com.webbudget.domain.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class FormPaymentPresenter {

    private final PaymentService paymentService;
    private final PeriodMovementRepository periodMovementRepository;
    private final WalletRepository walletRepository;
    private final CardRepository cardRepository;

    public Optional<PeriodMovement> findPeriodMovementById(long id) {
        return periodMovementRepository.findById(id);
    }

    public void pay(PeriodMovement periodMovement, Payment payment) {
        paymentService.pay(periodMovement, payment);
    }

    public List<Wallet> findActiveWallets() {
        return walletRepository.findAllActive();
    }

    public List<Card> findCreditCards() {
        return cardRepository.findByCardTypeAndActive(CardType.CREDIT, true);
    }

    public List<Card> findDebitCards() {
        return cardRepository.findByCardTypeAndActive(CardType.DEBIT, true);
    }
}
