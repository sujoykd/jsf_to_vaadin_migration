package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.PeriodMovement;
import br.com.webbudget.domain.entities.registration.Contact;
import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.infrastructure.i18n.MessageSource;
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
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.time.LocalDate;

@PermitAll
@Route(value = "financial/period-movements/form/:id?", layout = MainLayout.class)
@PageTitle("Period Movements")
public class FormPeriodMovementView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    final ComboBox<Contact> contactComboBox = new ComboBox<>("Contact");
    final TextField identificationField = new TextField("Identification");
    final ComboBox<FinancialPeriod> financialPeriodComboBox = new ComboBox<>("Financial Period");
    final DatePicker dueDatePicker = new DatePicker("Due Date");
    final BigDecimalField amountField = new BigDecimalField("Amount");
    final TextArea descriptionArea = new TextArea("Description");

    final Button saveButton = new Button("Save");
    final Button saveAndPayButton = new Button("Save & Pay");
    final Button updateButton = new Button("Update");
    final Button updateAndPayButton = new Button("Update & Pay");
    final Button backButton = new Button("Back");

    private boolean editMode = false;
    private PeriodMovement currentMovement;

    private final FormPeriodMovementPresenter presenter;

    public FormPeriodMovementView(FormPeriodMovementPresenter presenter) {
        this.presenter = presenter;

        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");

        contactComboBox.setItemLabelGenerator(c -> c != null ? c.getName() : "");
        contactComboBox.setItems(presenter.findActiveContacts());
        contactComboBox.setWidthFull();

        identificationField.setWidthFull();
        identificationField.setRequired(true);
        identificationField.setRequiredIndicatorVisible(true);

        financialPeriodComboBox.setItemLabelGenerator(fp -> fp != null ? fp.getIdentification() : "");
        financialPeriodComboBox.setItems(presenter.findOpenPeriods());
        financialPeriodComboBox.setWidthFull();
        financialPeriodComboBox.setRequired(true);
        financialPeriodComboBox.setRequiredIndicatorVisible(true);

        dueDatePicker.setWidthFull();
        dueDatePicker.setRequired(true);
        dueDatePicker.setRequiredIndicatorVisible(true);

        amountField.setWidthFull();
        amountField.setRequired(true);
        amountField.setRequiredIndicatorVisible(true);

        descriptionArea.setWidthFull();
        descriptionArea.setMinHeight("80px");

        var formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        formLayout.add(identificationField, contactComboBox);
        formLayout.add(financialPeriodComboBox, dueDatePicker);
        formLayout.add(amountField);
        formLayout.add(descriptionArea, 2);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> onSave());

        saveAndPayButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveAndPayButton.addClickListener(e -> onSaveAndPay());

        updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateButton.setVisible(false);
        updateButton.addClickListener(e -> onUpdate());

        updateAndPayButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateAndPayButton.setVisible(false);
        updateAndPayButton.addClickListener(e -> onUpdateAndPay());

        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("financial/period-movements"));

        var toolbar = new HorizontalLayout(saveButton, saveAndPayButton, updateButton, updateAndPayButton, backButton);
        toolbar.setSpacing(true);

        add(formLayout, toolbar);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var idParam = event.getRouteParameters().get("id");
        if (idParam.isPresent()) {
            long id = Long.parseLong(idParam.get());
            presenter.findById(id).ifPresentOrElse(pm -> {
                currentMovement = pm;
                editMode = true;
            }, () -> event.forwardTo("financial/period-movements"));
        } else {
            currentMovement = new PeriodMovement();
            editMode = false;
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        populateForm(currentMovement);
        saveButton.setVisible(!editMode);
        saveAndPayButton.setVisible(!editMode);
        updateButton.setVisible(editMode);
        updateAndPayButton.setVisible(editMode);
    }

    private void populateForm(PeriodMovement pm) {
        identificationField.setValue(pm.getIdentification() != null ? pm.getIdentification() : "");
        amountField.setValue(pm.getValue() != null ? pm.getValue() : BigDecimal.ZERO);
        dueDatePicker.setValue(pm.getDueDate() != null ? pm.getDueDate() : LocalDate.now());
        descriptionArea.setValue(pm.getDescription() != null ? pm.getDescription() : "");
        contactComboBox.setValue(pm.getContact());
        financialPeriodComboBox.setValue(pm.getFinancialPeriod());
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

        if (financialPeriodComboBox.getValue() == null) {
            financialPeriodComboBox.setInvalid(true);
            financialPeriodComboBox.setErrorMessage("Financial period is required");
            valid = false;
        } else {
            financialPeriodComboBox.setInvalid(false);
        }

        if (dueDatePicker.getValue() == null) {
            dueDatePicker.setInvalid(true);
            dueDatePicker.setErrorMessage("Due date is required");
            valid = false;
        } else {
            dueDatePicker.setInvalid(false);
        }

        if (amountField.getValue() == null) {
            amountField.setInvalid(true);
            amountField.setErrorMessage("Amount is required");
            valid = false;
        } else {
            amountField.setInvalid(false);
        }

        return valid;
    }

    private PeriodMovement buildEntity() {
        if (currentMovement == null) {
            currentMovement = new PeriodMovement();
        }
        currentMovement.setIdentification(identificationField.getValue());
        currentMovement.setValue(amountField.getValue());
        currentMovement.setDueDate(dueDatePicker.getValue());
        currentMovement.setDescription(descriptionArea.getValue().isBlank() ? null : descriptionArea.getValue());
        currentMovement.setContact(contactComboBox.getValue());
        currentMovement.setFinancialPeriod(financialPeriodComboBox.getValue());
        return currentMovement;
    }

    private void onSave() {
        if (!validateFields()) {
            return;
        }
        try {
            presenter.save(buildEntity());
            currentMovement = new PeriodMovement();
            populateForm(currentMovement);
            var notification = Notification.show("Period movement saved successfully.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(3000);
        } catch (BusinessLogicException ex) {
            var notification = Notification.show(MessageSource.get(ex.getMessage()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        }
    }

    private void onSaveAndPay() {
        if (!validateFields()) {
            return;
        }
        try {
            PeriodMovement saved = presenter.save(buildEntity());
            UI.getCurrent().navigate(FormPaymentView.class,
                    new RouteParameters("id", String.valueOf(saved.getId())));
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
            var notification = Notification.show("Period movement updated successfully.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(3000);
        } catch (BusinessLogicException ex) {
            var notification = Notification.show(MessageSource.get(ex.getMessage()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        }
    }

    private void onUpdateAndPay() {
        if (!validateFields()) {
            return;
        }
        try {
            PeriodMovement saved = presenter.update(buildEntity());
            UI.getCurrent().navigate(FormPaymentView.class,
                    new RouteParameters("id", String.valueOf(saved.getId())));
        } catch (BusinessLogicException ex) {
            var notification = Notification.show(MessageSource.get(ex.getMessage()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        }
    }
}
