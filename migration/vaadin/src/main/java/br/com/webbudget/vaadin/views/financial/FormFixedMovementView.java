package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.FixedMovement;
import br.com.webbudget.domain.entities.financial.FixedMovementState;
import br.com.webbudget.domain.entities.registration.Contact;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.infrastructure.i18n.MessageSource;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
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
@Route(value = "financial/fixed-movements/form/:id?", layout = MainLayout.class)
@PageTitle("Fixed Movements")
public class FormFixedMovementView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    final TextField identificationField = new TextField("Identification");
    final BigDecimalField valueField = new BigDecimalField("Value");
    final DatePicker startDatePicker = new DatePicker("Start");
    final Checkbox autoLaunchCheckbox = new Checkbox("Automatic Entry");
    final Checkbox undeterminedCheckbox = new Checkbox("Undetermined");
    final IntegerField totalQuotesField = new IntegerField("Total Installments");
    final IntegerField startingQuoteField = new IntegerField("Starting Installment");
    final TextArea descriptionArea = new TextArea("Description");
    final ComboBox<Contact> contactComboBox = new ComboBox<>("Contact");
    final ComboBox<FixedMovementState> stateComboBox = new ComboBox<>("Status");

    private final Button saveButton = new Button("Save");
    private final Button updateButton = new Button("Update");

    private boolean editMode = false;
    private FixedMovement currentFixed;

    private final FormFixedMovementPresenter presenter;

    public FormFixedMovementView(FormFixedMovementPresenter presenter) {
        this.presenter = presenter;

        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");

        stateComboBox.setItems(FixedMovementState.values());
        stateComboBox.setItemLabelGenerator(s -> s != null ? MessageSource.get(s.toString()) : "");

        contactComboBox.setItemLabelGenerator(c -> c != null ? c.getName() : "");
        contactComboBox.setItems(presenter.findContacts(null));

        undeterminedCheckbox.addValueChangeListener(e -> {
            totalQuotesField.setVisible(!e.getValue());
            startingQuoteField.setVisible(!e.getValue());
        });

        identificationField.setWidthFull();
        identificationField.setRequired(true);

        valueField.setWidthFull();
        valueField.setRequired(true);

        startDatePicker.setWidthFull();
        startDatePicker.setRequired(true);

        totalQuotesField.setWidthFull();
        startingQuoteField.setWidthFull();
        descriptionArea.setWidthFull();
        descriptionArea.setMinHeight("80px");
        contactComboBox.setWidthFull();
        stateComboBox.setWidthFull();

        var formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        formLayout.add(identificationField, valueField);
        formLayout.add(startDatePicker, contactComboBox);
        formLayout.add(stateComboBox);
        formLayout.add(autoLaunchCheckbox, undeterminedCheckbox);
        formLayout.add(totalQuotesField, startingQuoteField);
        formLayout.add(descriptionArea, 2);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> onSave());

        updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateButton.addClickListener(e -> onUpdate());
        updateButton.setVisible(false);

        var backButton = new Button("Back", e -> UI.getCurrent().navigate("financial/fixed-movements"));
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
            presenter.findById(id).ifPresentOrElse(fm -> {
                currentFixed = fm;
                editMode = true;
            }, () -> event.forwardTo("financial/fixed-movements"));
        } else {
            currentFixed = new FixedMovement();
            editMode = false;
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        populateForm(currentFixed);
        saveButton.setVisible(!editMode);
        updateButton.setVisible(editMode);
    }

    private void populateForm(FixedMovement fm) {
        identificationField.setValue(fm.getIdentification() != null ? fm.getIdentification() : "");
        valueField.setValue(fm.getValue() != null ? fm.getValue() : BigDecimal.ZERO);
        startDatePicker.setValue(fm.getStartDate());
        autoLaunchCheckbox.setValue(fm.isAutoLaunch());
        undeterminedCheckbox.setValue(fm.isUndetermined());
        totalQuotesField.setValue(fm.getTotalQuotes());
        startingQuoteField.setValue(fm.getStartingQuote());
        descriptionArea.setValue(fm.getDescription() != null ? fm.getDescription() : "");
        contactComboBox.setValue(fm.getContact());
        stateComboBox.setValue(fm.getFixedMovementState());

        boolean undetermined = fm.isUndetermined();
        totalQuotesField.setVisible(!undetermined);
        startingQuoteField.setVisible(!undetermined);
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
        if (valueField.getValue() == null) {
            valueField.setInvalid(true);
            valueField.setErrorMessage("Value is required");
            valid = false;
        } else {
            valueField.setInvalid(false);
        }
        if (startDatePicker.getValue() == null) {
            startDatePicker.setInvalid(true);
            startDatePicker.setErrorMessage("Start date is required");
            valid = false;
        } else {
            startDatePicker.setInvalid(false);
        }
        return valid;
    }

    private FixedMovement buildEntity() {
        if (currentFixed == null) {
            currentFixed = new FixedMovement();
        }
        currentFixed.setIdentification(identificationField.getValue());
        currentFixed.setValue(valueField.getValue());
        currentFixed.setStartDate(startDatePicker.getValue());
        currentFixed.setAutoLaunch(autoLaunchCheckbox.getValue());
        currentFixed.setUndetermined(undeterminedCheckbox.getValue());
        currentFixed.setDescription(descriptionArea.getValue());
        currentFixed.setContact(contactComboBox.getValue());
        currentFixed.setFixedMovementState(stateComboBox.getValue());

        if (!undeterminedCheckbox.getValue()) {
            currentFixed.setTotalQuotes(totalQuotesField.getValue());
            currentFixed.setStartingQuote(startingQuoteField.getValue());
        } else {
            currentFixed.setTotalQuotes(null);
            currentFixed.setStartingQuote(null);
        }

        return currentFixed;
    }

    private void onSave() {
        if (!validateFields()) {
            return;
        }
        try {
            presenter.save(buildEntity());
            currentFixed = new FixedMovement();
            populateForm(currentFixed);
            var notification = Notification.show("Fixed movement saved successfully.");
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
            var notification = Notification.show("Fixed movement updated successfully.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(3000);
        } catch (BusinessLogicException ex) {
            var notification = Notification.show(MessageSource.get(ex.getMessage()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        }
    }
}
