package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.Payment;
import br.com.webbudget.domain.entities.financial.PaymentMethod;
import br.com.webbudget.domain.entities.financial.PeriodMovement;
import br.com.webbudget.domain.entities.registration.Card;
import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.time.LocalDate;

@PermitAll
@Route(value = "financial/period-movements/pay/:id", layout = MainLayout.class)
@PageTitle("Payments")
public class FormPaymentView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    final DatePicker paidOnPicker = new DatePicker("Paid On");
    final BigDecimalField discountField = new BigDecimalField("Discount");
    final Select<PaymentMethod> paymentMethodSelect = new Select<>();
    final ComboBox<Wallet> walletComboBox = new ComboBox<>("Wallet");
    final ComboBox<Card> creditCardComboBox = new ComboBox<>("Credit Card");
    final ComboBox<Card> debitCardComboBox = new ComboBox<>("Debit Card");

    final Button payButton = new Button("Pay");
    final Button backButton = new Button("Back");

    private final TextField movementIdentificationField = new TextField("Movement");
    private final TextField movementValueField = new TextField("Value");

    private PeriodMovement currentMovement;

    private final FormPaymentPresenter presenter;

    public FormPaymentView(FormPaymentPresenter presenter) {
        this.presenter = presenter;

        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");

        // Movement info (read-only)
        movementIdentificationField.setReadOnly(true);
        movementIdentificationField.setWidthFull();

        movementValueField.setReadOnly(true);
        movementValueField.setWidthFull();

        // Payment date
        paidOnPicker.setValue(LocalDate.now());
        paidOnPicker.setRequired(true);
        paidOnPicker.setWidthFull();

        // Discount
        discountField.setValue(BigDecimal.ZERO);
        discountField.setWidthFull();

        // Payment method select
        paymentMethodSelect.setLabel("Payment Method");
        paymentMethodSelect.setItems(PaymentMethod.values());
        paymentMethodSelect.setItemLabelGenerator(PaymentMethod::name);
        paymentMethodSelect.setValue(PaymentMethod.CASH);
        paymentMethodSelect.setRequiredIndicatorVisible(true);
        paymentMethodSelect.setWidthFull();

        // Wallet combo
        walletComboBox.setItemLabelGenerator(w -> w != null ? w.getFullName() : "");
        walletComboBox.setItems(presenter.findActiveWallets());
        walletComboBox.setWidthFull();

        // Credit card combo
        creditCardComboBox.setItemLabelGenerator(c -> c != null ? c.getReadableName() : "");
        creditCardComboBox.setItems(presenter.findCreditCards());
        creditCardComboBox.setWidthFull();
        creditCardComboBox.setVisible(false);

        // Debit card combo
        debitCardComboBox.setItemLabelGenerator(c -> c != null ? c.getReadableName() : "");
        debitCardComboBox.setItems(presenter.findDebitCards());
        debitCardComboBox.setWidthFull();
        debitCardComboBox.setVisible(false);

        // Visibility listener
        paymentMethodSelect.addValueChangeListener(e -> updateFieldVisibility(e.getValue()));

        // Form layout
        var formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        formLayout.add(movementIdentificationField, movementValueField);
        formLayout.add(paidOnPicker, discountField);
        formLayout.add(paymentMethodSelect, walletComboBox);
        formLayout.add(creditCardComboBox, debitCardComboBox);

        // Buttons
        payButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        payButton.addClickListener(e -> onPay());

        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("financial/period-movements"));

        var toolbar = new HorizontalLayout(payButton, backButton);
        toolbar.setSpacing(true);

        add(formLayout, toolbar);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var idParam = event.getRouteParameters().get("id");
        if (idParam.isEmpty()) {
            event.forwardTo("financial/period-movements");
            return;
        }
        long id = Long.parseLong(idParam.get());
        presenter.findPeriodMovementById(id).ifPresentOrElse(pm -> {
            currentMovement = pm;
        }, () -> event.forwardTo("financial/period-movements"));
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        if (currentMovement != null) {
            populateMovementInfo(currentMovement);
        }
    }

    private void populateMovementInfo(PeriodMovement pm) {
        movementIdentificationField.setValue(pm.getIdentification() != null ? pm.getIdentification() : "");
        movementValueField.setValue(pm.getValue() != null ? pm.getValue().toPlainString() : "");
    }

    private void updateFieldVisibility(PaymentMethod method) {
        if (method == null) {
            return;
        }
        switch (method) {
            case CASH -> {
                walletComboBox.setVisible(true);
                creditCardComboBox.setVisible(false);
                debitCardComboBox.setVisible(false);
            }
            case CREDIT_CARD -> {
                walletComboBox.setVisible(false);
                creditCardComboBox.setVisible(true);
                debitCardComboBox.setVisible(false);
            }
            case DEBIT_CARD -> {
                walletComboBox.setVisible(false);
                creditCardComboBox.setVisible(false);
                debitCardComboBox.setVisible(true);
            }
        }
    }

    private boolean validateFields() {
        boolean valid = true;
        if (paidOnPicker.getValue() == null) {
            paidOnPicker.setInvalid(true);
            paidOnPicker.setErrorMessage("Paid on date is required");
            valid = false;
        } else {
            paidOnPicker.setInvalid(false);
        }
        if (paymentMethodSelect.getValue() == null) {
            paymentMethodSelect.setInvalid(true);
            paymentMethodSelect.setErrorMessage("Payment method is required");
            valid = false;
        } else {
            paymentMethodSelect.setInvalid(false);
        }
        return valid;
    }

    private Payment buildPayment() {
        var payment = new Payment();
        payment.setPaidOn(paidOnPicker.getValue());
        payment.setDiscount(discountField.getValue() != null ? discountField.getValue() : BigDecimal.ZERO);
        payment.setPaymentMethod(paymentMethodSelect.getValue());

        switch (paymentMethodSelect.getValue()) {
            case CASH -> payment.setWallet(walletComboBox.getValue());
            case CREDIT_CARD -> payment.setCard(creditCardComboBox.getValue());
            case DEBIT_CARD -> payment.setCard(debitCardComboBox.getValue());
        }

        return payment;
    }

    private void onPay() {
        if (!validateFields()) {
            return;
        }
        presenter.pay(currentMovement, buildPayment());
        var notification = Notification.show("Payment registered successfully.");
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setDuration(3000);
        UI.getCurrent().navigate("financial/period-movements");
    }
}
