package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.infrastructure.i18n.MessageSource;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

@PermitAll
@Route(value = "registration/financial-periods/form/:id?", layout = MainLayout.class)
@PageTitle("Financial Periods")
public class FormFinancialPeriodView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    final TextField identificationField = new TextField("Identification");
    final DatePicker startDatePicker = new DatePicker("Start");
    final DatePicker endDatePicker = new DatePicker("End");
    final BigDecimalField creditCardGoalField = new BigDecimalField("Credit Card Goal");
    final BigDecimalField expensesGoalField = new BigDecimalField("Expenses Goal");
    final BigDecimalField incomesGoalField = new BigDecimalField("Income Goal");

    final Button saveButton = new Button("Save");
    final Button backButton = new Button("Back");

    private boolean editMode = false;
    private FinancialPeriod currentFinancialPeriod;

    private final FormFinancialPeriodPresenter presenter;

    public FormFinancialPeriodView(FormFinancialPeriodPresenter presenter) {
        this.presenter = presenter;

        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");

        identificationField.setWidthFull();
        identificationField.setRequired(true);

        startDatePicker.setWidthFull();
        startDatePicker.setRequired(true);

        endDatePicker.setWidthFull();
        endDatePicker.setRequired(true);

        creditCardGoalField.setWidthFull();
        creditCardGoalField.setRequired(true);

        expensesGoalField.setWidthFull();
        expensesGoalField.setRequired(true);

        incomesGoalField.setWidthFull();
        incomesGoalField.setRequired(true);

        var formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        formLayout.add(identificationField, 2);
        formLayout.add(startDatePicker, endDatePicker);
        formLayout.add(creditCardGoalField, expensesGoalField);
        formLayout.add(incomesGoalField);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> onSave());

        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("registration/financial-periods"));

        var toolbar = new HorizontalLayout(saveButton, backButton);
        toolbar.setSpacing(true);

        add(formLayout, toolbar);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var idParam = event.getRouteParameters().get("id");
        if (idParam.isPresent()) {
            long id = Long.parseLong(idParam.get());
            presenter.findById(id).ifPresentOrElse(fp -> {
                currentFinancialPeriod = fp;
                editMode = true;
            }, () -> event.forwardTo("registration/financial-periods"));
        } else {
            currentFinancialPeriod = new FinancialPeriod();
            editMode = false;
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        populateForm(currentFinancialPeriod);
        setReadOnly(editMode);
        saveButton.setVisible(!editMode);
    }

    private void populateForm(FinancialPeriod fp) {
        identificationField.setValue(fp.getIdentification() != null ? fp.getIdentification() : "");
        startDatePicker.setValue(fp.getStart());
        endDatePicker.setValue(fp.getEnd());
        creditCardGoalField.setValue(fp.getCreditCardGoal() != null ? fp.getCreditCardGoal() : BigDecimal.ZERO);
        expensesGoalField.setValue(fp.getExpensesGoal() != null ? fp.getExpensesGoal() : BigDecimal.ZERO);
        incomesGoalField.setValue(fp.getRevenuesGoal() != null ? fp.getRevenuesGoal() : BigDecimal.ZERO);
    }

    private void setReadOnly(boolean readOnly) {
        identificationField.setReadOnly(readOnly);
        startDatePicker.setReadOnly(readOnly);
        endDatePicker.setReadOnly(readOnly);
        creditCardGoalField.setReadOnly(readOnly);
        expensesGoalField.setReadOnly(readOnly);
        incomesGoalField.setReadOnly(readOnly);
    }

    private boolean validateFields() {
        boolean valid = true;
        if (identificationField.getValue() == null || identificationField.getValue().isBlank()) {
            identificationField.setInvalid(true);
            identificationField.setErrorMessage("Identification is required");
            valid = false;
        } else {
            identificationField.setInvalid(false);
        }
        if (startDatePicker.getValue() == null) {
            startDatePicker.setInvalid(true);
            startDatePicker.setErrorMessage("Start date is required");
            valid = false;
        } else {
            startDatePicker.setInvalid(false);
        }
        if (endDatePicker.getValue() == null) {
            endDatePicker.setInvalid(true);
            endDatePicker.setErrorMessage("End date is required");
            valid = false;
        } else {
            endDatePicker.setInvalid(false);
        }
        if (creditCardGoalField.getValue() == null) {
            creditCardGoalField.setInvalid(true);
            creditCardGoalField.setErrorMessage("Credit card goal is required");
            valid = false;
        } else {
            creditCardGoalField.setInvalid(false);
        }
        if (expensesGoalField.getValue() == null) {
            expensesGoalField.setInvalid(true);
            expensesGoalField.setErrorMessage("Expenses goal is required");
            valid = false;
        } else {
            expensesGoalField.setInvalid(false);
        }
        if (incomesGoalField.getValue() == null) {
            incomesGoalField.setInvalid(true);
            incomesGoalField.setErrorMessage("Income goal is required");
            valid = false;
        } else {
            incomesGoalField.setInvalid(false);
        }
        return valid;
    }

    private FinancialPeriod buildEntity() {
        var fp = new FinancialPeriod();
        fp.setIdentification(identificationField.getValue());
        fp.setStart(startDatePicker.getValue());
        fp.setEnd(endDatePicker.getValue());
        fp.setCreditCardGoal(creditCardGoalField.getValue());
        fp.setExpensesGoal(expensesGoalField.getValue());
        fp.setRevenuesGoal(incomesGoalField.getValue());
        return fp;
    }

    private void onSave() {
        if (!validateFields()) {
            return;
        }
        try {
            presenter.save(buildEntity());
            populateForm(new FinancialPeriod());
            var notification = Notification.show("Financial period saved successfully.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(3000);
        } catch (BusinessLogicException ex) {
            var notification = Notification.show(MessageSource.get(ex.getMessage()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        }
    }
}
