package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.Card;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "registration/cards/detail/:id", layout = MainLayout.class)
@PageTitle("Cards")
public class DetailCardView extends VerticalLayout implements BeforeEnterObserver {

    final Checkbox activeCheckbox = new Checkbox("Active");
    final TextField nameField = new TextField("Name");
    final TextField typeField = new TextField("Type");
    final TextField brandField = new TextField("Brand");
    final TextField numberField = new TextField("Number");
    final TextField holderField = new TextField("Holder");
    final TextField creditLimitField = new TextField("Credit Limit");
    final TextField expirationDayField = new TextField("Expiration Day");
    final Button editButton = new Button("Edit");
    final Button deleteButton = new Button("Delete");
    final Button backButton = new Button("Back");

    private Card currentCard;
    private final DetailCardPresenter presenter;

    public DetailCardView(DetailCardPresenter presenter) {
        this.presenter = presenter;

        setPadding(true);
        setSpacing(true);
        setMaxWidth("900px");

        activeCheckbox.setEnabled(false);
        nameField.setReadOnly(true); nameField.setWidthFull();
        typeField.setReadOnly(true); typeField.setWidthFull();
        brandField.setReadOnly(true); brandField.setWidthFull();
        numberField.setReadOnly(true); numberField.setWidthFull();
        holderField.setReadOnly(true); holderField.setWidthFull();
        creditLimitField.setReadOnly(true); creditLimitField.setWidthFull();
        expirationDayField.setReadOnly(true); expirationDayField.setWidthFull();

        var formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));
        formLayout.add(activeCheckbox);
        formLayout.add(nameField, typeField);
        formLayout.add(brandField, numberField);
        formLayout.add(holderField, creditLimitField);
        formLayout.add(expirationDayField);

        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e -> {
            if (currentCard != null) UI.getCurrent().navigate(FormCardView.class, new RouteParameters("id", String.valueOf(currentCard.getId())));
        });

        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> handleDelete());

        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("registration/cards"));

        add(formLayout, new HorizontalLayout(editButton, deleteButton, backButton));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("id")
                .map(Long::parseLong)
                .flatMap(presenter::findById)
                .ifPresentOrElse(this::loadCard, () -> event.forwardTo("registration/cards"));
    }

    private void loadCard(Card card) {
        currentCard = card;
        activeCheckbox.setValue(card.isActive());
        nameField.setValue(card.getName() != null ? card.getName() : "");
        typeField.setValue(card.getCardType() != null ? card.getCardType().name() : "");
        brandField.setValue(card.getFlag() != null ? card.getFlag() : "");
        numberField.setValue(card.getNumber() != null ? card.getNumber() : "");
        holderField.setValue(card.getOwner() != null ? card.getOwner() : "");
        creditLimitField.setValue(card.getCreditLimit() != null ? card.getCreditLimit().toPlainString() : "");
        expirationDayField.setValue(card.getExpirationDay() != null ? card.getExpirationDay().toString() : "");
    }

    private void handleDelete() {
        if (currentCard == null) return;
        var dialog = new ConfirmDialog();
        dialog.setHeader("Confirm Delete");
        dialog.setText("Delete card '" + currentCard.getName() + "'?");
        dialog.setCancelable(true);
        dialog.setCancelText("No");
        dialog.setConfirmText("Yes");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(ev -> {
            presenter.delete(currentCard);
            UI.getCurrent().navigate("registration/cards");
        });
        dialog.open();
    }
}
