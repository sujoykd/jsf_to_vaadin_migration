package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.financial.CreditCardInvoice;
import br.com.webbudget.domain.entities.registration.Card;
import br.com.webbudget.domain.entities.view.CardConsumeDetailed;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "registration/cards/statistics/:id", layout = MainLayout.class)
@PageTitle("Statistics")
public class CardStatisticsView extends VerticalLayout implements BeforeEnterObserver {

    final TextField cardNameField = new TextField("Card");
    final TextField cardNumberField = new TextField("Number");
    final TextField cardTypeField = new TextField("Type");
    final Grid<CreditCardInvoice> invoicesGrid = new Grid<>(CreditCardInvoice.class, false);
    final Grid<CardConsumeDetailed> detailedConsumeGrid = new Grid<>(CardConsumeDetailed.class, false);
    final Button backButton = new Button("Back");

    private final CardStatisticsPresenter presenter;

    public CardStatisticsView(CardStatisticsPresenter presenter) {
        this.presenter = presenter;

        setPadding(true);
        setSpacing(true);
        setMaxWidth("900px");

        cardNameField.setReadOnly(true);
        cardNameField.setWidthFull();
        cardNumberField.setReadOnly(true);
        cardNumberField.setWidthFull();
        cardTypeField.setReadOnly(true);
        cardTypeField.setWidthFull();

        var cardInfo = new HorizontalLayout(cardNameField, cardNumberField, cardTypeField);
        cardInfo.setWidthFull();

        invoicesGrid.addColumn(inv -> inv.getIdentification() != null ? inv.getIdentification() : "")
                .setHeader("Identification");
        invoicesGrid.addColumn(inv -> inv.getDueDate() != null ? inv.getDueDate().toString() : "")
                .setHeader("Due Date");
        invoicesGrid.addColumn(inv -> inv.getTotalValue() != null ? inv.getTotalValue().toPlainString() : "")
                .setHeader("Total Value");
        invoicesGrid.addColumn(inv -> inv.getInvoiceState() != null ? inv.getInvoiceState().name() : "")
                .setHeader("State");
        invoicesGrid.setHeight("200px");

        detailedConsumeGrid.addColumn(CardConsumeDetailed::getCostCenter).setHeader("Cost Center");
        detailedConsumeGrid.addColumn(CardConsumeDetailed::getMovementClass).setHeader("Movement Class");
        detailedConsumeGrid.addColumn(c -> c.getValue() != null ? c.getValue().toPlainString() : "")
                .setHeader("Value");
        detailedConsumeGrid.setHeight("200px");

        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("registration/cards"));

        add(cardInfo,
                new H3("Last Invoices"), invoicesGrid,
                new H3("Consume by Movement Class"), detailedConsumeGrid,
                new HorizontalLayout(backButton));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("id")
                .map(Long::parseLong)
                .flatMap(presenter::findById)
                .ifPresentOrElse(this::loadCard,
                        () -> event.forwardTo("registration/cards"));
    }

    private void loadCard(Card card) {
        cardNameField.setValue(card.getName() != null ? card.getName() : "");
        cardNumberField.setValue(card.getNumber() != null ? card.getNumber() : "");
        cardTypeField.setValue(card.getCardType() != null ? card.getCardType().name() : "");
        invoicesGrid.setItems(presenter.findInvoicesByCard(card));
        detailedConsumeGrid.setItems(presenter.findDetailedConsumeByCardId(card.getId()));
    }
}
