package br.com.webbudget.application.components.builder;

import br.com.webbudget.domain.entities.financial.CreditCardInvoice;
import br.com.webbudget.domain.entities.registration.Card;
import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.infrastructure.i18n.MessageSource;

import java.time.LocalDate;

import static br.com.webbudget.infrastructure.utils.RandomCode.numeric;

public class CreditCardInvoiceBuilder extends AbstractBuilder<CreditCardInvoice> {

    public CreditCardInvoiceBuilder() {
        this.instance = new CreditCardInvoice();
    }

    public CreditCardInvoiceBuilder card(Card card) {
        this.instance.setCard(card);
        return this;
    }

    public CreditCardInvoiceBuilder financialPeriod(FinancialPeriod financialPeriod) {
        this.instance.setFinancialPeriod(financialPeriod);
        return this;
    }

    @Override
    public CreditCardInvoice build() {
        this.instance.setIdentification(this.defineIdentification());
        this.instance.setDueDate(this.defineDueDate());
        return this.instance;
    }

    private String defineIdentification() {
        final Card card = this.instance.getCard();
        return MessageSource.get("credit-card-invoice.invoice-title", numeric(4), card.getName(),
                card.getNumber().substring(card.getNumber().length() - 4));
    }

    private LocalDate defineDueDate() {
        return this.instance.getFinancialPeriod().getEnd().plusMonths(1).withDayOfMonth(
                this.instance.getCard().getExpirationDay());
    }
}
