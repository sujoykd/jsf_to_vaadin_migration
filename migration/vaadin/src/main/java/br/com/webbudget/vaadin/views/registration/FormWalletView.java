package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.domain.entities.registration.WalletType;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.infrastructure.i18n.MessageSource;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
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
@Route(value = "registration/wallets/form/:id?", layout = MainLayout.class)
@PageTitle("Wallets")
public class FormWalletView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    final Checkbox activeCheckbox = new Checkbox("Active");
    final TextField nameField = new TextField("Name");
    final Select<WalletType> walletTypeSelect = new Select<>();
    final TextField bankField = new TextField("Bank");
    final TextField agencyField = new TextField("Branch");
    final TextField accountField = new TextField("Account");
    final TextField digitField = new TextField("Check Digit");
    final BigDecimalField actualBalanceField = new BigDecimalField("Balance");
    final TextArea descriptionArea = new TextArea("Description");

    final Button saveButton = new Button("Save");
    final Button updateButton = new Button("Update");
    final Button backButton = new Button("Back");

    private boolean editMode = false;
    private Wallet currentWallet;

    private final FormWalletPresenter presenter;

    public FormWalletView(FormWalletPresenter presenter) {
        this.presenter = presenter;

        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");

        walletTypeSelect.setLabel("Wallet Type");
        walletTypeSelect.setItems(WalletType.values());
        walletTypeSelect.setItemLabelGenerator(wt -> wt != null ? MessageSource.get(wt.toString()) : "");
        walletTypeSelect.setWidthFull();
        walletTypeSelect.setRequiredIndicatorVisible(true);

        nameField.setWidthFull();
        nameField.setRequired(true);
        nameField.setRequiredIndicatorVisible(true);

        actualBalanceField.setWidthFull();
        actualBalanceField.setRequired(true);
        actualBalanceField.setRequiredIndicatorVisible(true);

        bankField.setWidthFull();
        agencyField.setWidthFull();
        accountField.setWidthFull();
        digitField.setWidthFull();
        descriptionArea.setWidthFull();
        descriptionArea.setMinHeight("80px");

        var formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        formLayout.add(activeCheckbox, 2);
        formLayout.add(nameField, walletTypeSelect);
        formLayout.add(bankField, agencyField);
        formLayout.add(accountField, digitField);
        formLayout.add(actualBalanceField);
        formLayout.add(descriptionArea, 2);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> onSave());

        updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateButton.addClickListener(e -> onUpdate());
        updateButton.setVisible(false);

        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("registration/wallets"));

        var toolbar = new HorizontalLayout(saveButton, updateButton, backButton);
        toolbar.setSpacing(true);

        add(formLayout, toolbar);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var idParam = event.getRouteParameters().get("id");
        if (idParam.isPresent()) {
            long id = Long.parseLong(idParam.get());
            presenter.findById(id).ifPresentOrElse(wallet -> {
                currentWallet = wallet;
                editMode = true;
            }, () -> event.forwardTo("registration/wallets"));
        } else {
            currentWallet = new Wallet();
            editMode = false;
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        populateForm(currentWallet);
        saveButton.setVisible(!editMode);
        updateButton.setVisible(editMode);
    }

    private void populateForm(Wallet wallet) {
        activeCheckbox.setValue(wallet.isActive());
        nameField.setValue(wallet.getName() != null ? wallet.getName() : "");
        walletTypeSelect.setValue(wallet.getWalletType());
        bankField.setValue(wallet.getBank() != null ? wallet.getBank() : "");
        agencyField.setValue(wallet.getAgency() != null ? wallet.getAgency() : "");
        accountField.setValue(wallet.getAccount() != null ? wallet.getAccount() : "");
        digitField.setValue(wallet.getDigit() != null ? wallet.getDigit() : "");
        actualBalanceField.setValue(wallet.getActualBalance() != null ? wallet.getActualBalance() : BigDecimal.ZERO);
        descriptionArea.setValue(wallet.getDescription() != null ? wallet.getDescription() : "");
    }

    private boolean validateFields() {
        boolean valid = true;
        if (nameField.getValue() == null || nameField.getValue().isBlank()) {
            nameField.setInvalid(true);
            nameField.setErrorMessage("Name is required");
            valid = false;
        } else {
            nameField.setInvalid(false);
        }
        if (walletTypeSelect.getValue() == null) {
            walletTypeSelect.setInvalid(true);
            walletTypeSelect.setErrorMessage("Wallet Type is required");
            valid = false;
        } else {
            walletTypeSelect.setInvalid(false);
        }
        if (actualBalanceField.getValue() == null) {
            actualBalanceField.setInvalid(true);
            actualBalanceField.setErrorMessage("Balance is required");
            valid = false;
        } else {
            actualBalanceField.setInvalid(false);
        }
        return valid;
    }

    private Wallet buildEntity() {
        if (currentWallet == null) {
            currentWallet = new Wallet();
        }
        currentWallet.setActive(activeCheckbox.getValue());
        currentWallet.setName(nameField.getValue());
        currentWallet.setWalletType(walletTypeSelect.getValue());
        currentWallet.setBank(bankField.getValue().isBlank() ? null : bankField.getValue());
        currentWallet.setAgency(agencyField.getValue().isBlank() ? null : agencyField.getValue());
        currentWallet.setAccount(accountField.getValue().isBlank() ? null : accountField.getValue());
        currentWallet.setDigit(digitField.getValue().isBlank() ? null : digitField.getValue());
        currentWallet.setActualBalance(actualBalanceField.getValue());
        currentWallet.setDescription(descriptionArea.getValue().isBlank() ? null : descriptionArea.getValue());
        return currentWallet;
    }

    private void onSave() {
        if (!validateFields()) {
            return;
        }
        try {
            presenter.save(buildEntity());
            currentWallet = new Wallet();
            populateForm(currentWallet);
            var notification = Notification.show("Wallet saved successfully.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(3000);
        } catch (BusinessLogicException ex) {
            var notification = Notification.show(MessageSource.get(ex.getMessage()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        }
    }

    private void onUpdate() {
        if (!validateFields()) {
            return;
        }
        try {
            presenter.update(buildEntity());
            var notification = Notification.show("Wallet updated successfully.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(3000);
        } catch (BusinessLogicException ex) {
            var notification = Notification.show(MessageSource.get(ex.getMessage()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        }
    }
}
