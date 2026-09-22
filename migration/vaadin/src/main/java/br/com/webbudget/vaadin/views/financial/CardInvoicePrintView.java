package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.CreditCardInvoice;
import br.com.webbudget.domain.entities.financial.PeriodMovement;
import br.com.webbudget.domain.repositories.financial.CreditCardInvoiceRepository;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@PermitAll
@Route("financial/card-invoice-print/:id")
@PageTitle("Print Invoice")
public class CardInvoicePrintView extends VerticalLayout implements BeforeEnterObserver {

    private final CreditCardInvoiceRepository creditCardInvoiceRepository;

    final Button printButton;
    final Grid<PeriodMovement> movementsGrid;
    final Span cardNameSpan;
    final Span identificationSpan;
    final Span totalSpan;

    public CardInvoicePrintView(CreditCardInvoiceRepository creditCardInvoiceRepository) {
        this.creditCardInvoiceRepository = creditCardInvoiceRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        cardNameSpan = new Span();
        identificationSpan = new Span();
        totalSpan = new Span();

        printButton = new Button("Print", e -> UI.getCurrent().getPage().executeJs("window.print()"));
        printButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        movementsGrid = new Grid<>(PeriodMovement.class, false);
        movementsGrid.addColumn(PeriodMovement::getIdentification).setHeader("Description").setSortable(false);
        movementsGrid.addColumn(movement -> {
            BigDecimal value = movement.getValueWithDiscount();
            return NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")).format(value);
        }).setHeader("Amount").setSortable(false);
        movementsGrid.setSizeFull();

        var header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);

        var cardLabel = new H2();
        cardLabel.add(new Span("Card: "), cardNameSpan);

        var identificationLabel = new H3();
        identificationLabel.add(new Span("Invoice: "), identificationSpan);

        var totalLabel = new Paragraph();
        totalLabel.add(new Span("Total: "), totalSpan);

        header.add(cardLabel, identificationLabel, totalLabel);

        var toolbar = new HorizontalLayout(printButton);
        toolbar.setWidthFull();

        add(toolbar, header, movementsGrid);
        expand(movementsGrid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("id").map(Long::parseLong).ifPresent(id -> {
            Optional<CreditCardInvoice> invoiceOpt = creditCardInvoiceRepository.findById(id);
            if (invoiceOpt.isPresent()) {
                CreditCardInvoice invoice = invoiceOpt.get();
                cardNameSpan.setText(invoice.getCard() != null ? invoice.getCard().getName() : "");
                identificationSpan.setText(invoice.getIdentification() != null ? invoice.getIdentification() : "");

                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
                totalSpan.setText(currencyFormat.format(invoice.getTotalValue()));

                List<PeriodMovement> movements = invoice.getPeriodMovements();
                movementsGrid.setItems(movements);
            }
        });
    }
}
