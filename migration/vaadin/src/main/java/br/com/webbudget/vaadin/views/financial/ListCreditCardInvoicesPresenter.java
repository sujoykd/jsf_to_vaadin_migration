package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.financial.CreditCardInvoice;
import br.com.webbudget.domain.entities.financial.InvoiceState;
import br.com.webbudget.domain.repositories.financial.CreditCardInvoiceRepository;
import br.com.webbudget.domain.services.CreditCardInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ListCreditCardInvoicesPresenter {

    private final CreditCardInvoiceRepository creditCardInvoiceRepository;
    private final CreditCardInvoiceService creditCardInvoiceService;

    public Page<CreditCardInvoice> findAll(String filter, InvoiceState state, int offset, int limit) {
        return creditCardInvoiceRepository.findAllBy(filter, state, offset, limit);
    }

    public int count(String filter, InvoiceState state) {
        return (int) creditCardInvoiceRepository.count(
                creditCardInvoiceRepository.buildSpecification(filter, state));
    }

    public void close(long invoiceId) {
        creditCardInvoiceService.close(invoiceId);
    }
}
