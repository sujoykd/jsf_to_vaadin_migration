package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.CreditCardInvoice;
import br.com.webbudget.domain.repositories.financial.CreditCardInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class DetailCreditCardInvoicePresenter {

    private final CreditCardInvoiceRepository creditCardInvoiceRepository;

    public Optional<CreditCardInvoice> findById(long id) {
        return creditCardInvoiceRepository.findById(id);
    }
}
