package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.Card;
import br.com.webbudget.domain.entities.registration.CardType;
import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.infrastructure.i18n.MessageSource;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;

@PermitAll
@Route(value = "registration/cards/form/:id?", layout = MainLayout.class)
@PageTitle("Cards")
public class FormCardView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    final Checkbox activeCheckbox = new Checkbox("Active");
    final TextField nameField = new TextField("Name");
    final Select<CardType> typeSelect = new Select<>();
    final TextField brandField = new TextField("Brand");
    final TextField numberField = new TextField("Number");
    final TextField holderField = new TextField("Holder");
    final BigDecimalField creditLimitField = new BigDecimalField("Credit Limit");
    final IntegerField expirationDayField = new IntegerField("Expiration Day");
    final ComboBox<Wallet> walletComboBox = new ComboBox<>("Debit Wallet");

    final Button saveButton = new Button("Save");
    final Button updateButton = new Button("Update");
    final Button backButton = new Button("Back", e -> UI.getCurrent().navigate("registration/cards"));

    private boolean editMode = false;
    private Card currentCard;

    private final FormCardPresenter presenter;

    public FormCardView(FormCardPresenter presenter) {
        this.presenter = presenter;

        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");

        typeSelect.setLabel("Type");
        typeSelect.setItems(CardType.values());
        typeSelect.setItemLabelGenerator(ct -> ct != null ? MessageSource.get(ct.toString()) : "");
        typeSelect.setRequiredIndicatorVisible(true);
        typeSelect.setWidthFull();

        walletComboBox.setItemLabelGenerator(w -> w != null ? w.getName() : "");
        walletComboBox.setItems(presenter.findActiveWallets());
        walletComboBox.setWidthFull();

        nameField.setRequired(true);
        nameField.setWidthFull();

        brandField.setRequired(true);
        brandField.setWidthFull();

        numberField.setRequired(true);
        numberField.setWidthFull();

        holderField.setRequired(true);
        holderField.setWidthFull();

        creditLimitField.setWidthFull();

        expirationDayField.setMin(1);
        expirationDayField.setMax(31);
        expirationDayField.setWidthFull();

        var formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        formLayout.add(nameField, typeSelect);
        formLayout.add(brandField, numberField);
        formLayout.add(holderField, creditLimitField);
        formLayout.add(expirationDayField, walletComboBox);
        formLayout.add(activeCheckbox);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> onSave());

        updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateButton.addClickListener(e -> onUpdate());
        updateButton.setVisible(false);

        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        var toolbar = new HorizontalLayout(saveButton, updateButton, backButton);
        toolbar.setSpacing(true);

        add(formLayout, toolbar);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var idParam = event.getRouteParameters().get("id");
        if (idParam.isPresent()) {
            long id = Long.parseLong(idParam.get());
            presenter.findById(id).ifPresentOrElse(card -> {
                currentCard = card;
                editMode = true;
            }, () -> event.forwardTo("registration/cards"));
        } else {
            currentCard = new Card();
            editMode = false;
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        populateForm(currentCard);
        saveButton.setVisible(!editMode);
        updateButton.setVisible(editMode);
    }

    private void populateForm(Card card) {
        nameField.setValue(card.getName() != null ? card.getName() : "");
        typeSelect.setValue(card.getCardType());
        brandField.setValue(card.getFlag() != null ? card.getFlag() : "");
        numberField.setValue(card.getNumber() != null ? card.getNumber() : "");
        holderField.setValue(card.getOwner() != null ? card.getOwner() : "");
        creditLimitField.setValue(card.getCreditLimit() != null ? card.getCreditLimit() : BigDecimal.ZERO);
        expirationDayField.setValue(card.getExpirationDay());
        walletComboBox.setValue(card.getWallet());
        activeCheckbox.setValue(card.isActive());
    }

    private boolean validate() {
        boolean valid = true;
        if (nameField.getValue() == null || nameField.getValue().isBlank()) {
            nameField.setInvalid(true);
            nameField.setErrorMessage("Name is required");
            valid = false;
        } else {
            nameField.setInvalid(false);
        }
        if (typeSelect.getValue() == null) {
            typeSelect.setInvalid(true);
            typeSelect.setErrorMessage("Type is required");
            valid = false;
        } else {
            typeSelect.setInvalid(false);
        }
        if (brandField.getValue() == null || brandField.getValue().isBlank()) {
            brandField.setInvalid(true);
            brandField.setErrorMessage("Brand is required");
            valid = false;
        } else {
            brandField.setInvalid(false);
        }
        if (numberField.getValue() == null || numberField.getValue().isBlank()) {
            numberField.setInvalid(true);
            numberField.setErrorMessage("Number is required");
            valid = false;
        } else {
            numberField.setInvalid(false);
        }
        if (holderField.getValue() == null || holderField.getValue().isBlank()) {
            holderField.setInvalid(true);
            holderField.setErrorMessage("Holder is required");
            valid = false;
        } else {
            holderField.setInvalid(false);
        }
        CardType selectedType = typeSelect.getValue();
        if (selectedType == CardType.DEBIT && walletComboBox.getValue() == null) {
            walletComboBox.setInvalid(true);
            walletComboBox.setErrorMessage("Debit card requires a wallet");
            valid = false;
        } else {
            walletComboBox.setInvalid(false);
        }
        if (selectedType == CardType.CREDIT
                && (expirationDayField.getValue() == null || expirationDayField.getValue() == 0)) {
            expirationDayField.setInvalid(true);
            expirationDayField.setErrorMessage("Credit card requires an expiration day (1-31)");
            valid = false;
        } else {
            expirationDayField.setInvalid(false);
        }
        return valid;
    }

    private Card buildEntity() {
        if (currentCard == null) {
            currentCard = new Card();
        }
        currentCard.setName(nameField.getValue());
        currentCard.setCardType(typeSelect.getValue());
        currentCard.setFlag(brandField.getValue());
        currentCard.setNumber(numberField.getValue());
        currentCard.setOwner(holderField.getValue());
        currentCard.setCreditLimit(creditLimitField.getValue());
        currentCard.setExpirationDay(expirationDayField.getValue());
        currentCard.setWallet(walletComboBox.getValue());
        currentCard.setActive(activeCheckbox.getValue());
        return currentCard;
    }

    private void onSave() {
        if (!validate()) {
            return;
        }
        try {
            presenter.save(buildEntity());
            currentCard = new Card();
            populateForm(currentCard);
            var notification = Notification.show("Card saved successfully.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(3000);
        } catch (BusinessLogicException ex) {
            var notification = Notification.show(MessageSource.get(ex.getMessage()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        }
    }

    private void onUpdate() {
        if (!validate()) {
            return;
        }
        try {
            presenter.update(buildEntity());
            var notification = Notification.show("Card updated successfully.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(3000);
        } catch (BusinessLogicException ex) {
            var notification = Notification.show(MessageSource.get(ex.getMessage()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        }
    }
}
