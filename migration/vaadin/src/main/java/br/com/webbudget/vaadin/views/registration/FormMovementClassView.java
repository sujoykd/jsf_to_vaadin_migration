package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.domain.entities.registration.MovementClass;
import br.com.webbudget.domain.entities.registration.MovementClassType;
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
@Route(value = "registration/movement-classes/form/:id?", layout = MainLayout.class)
@PageTitle("Movement Classes")
public class FormMovementClassView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    final Checkbox activeCheckbox = new Checkbox("Active");
    final TextField nameField = new TextField("Name");
    final Select<MovementClassType> movementClassTypeSelect = new Select<>();
    final ComboBox<CostCenter> costCenterComboBox = new ComboBox<>("Cost Center");
    final BigDecimalField budgetField = new BigDecimalField("Budget");

    final Button saveButton = new Button("Save");
    final Button updateButton = new Button("Update");
    final Button backButton = new Button("Back");

    private boolean editMode = false;
    private MovementClass currentMovementClass;

    private final FormMovementClassPresenter presenter;

    public FormMovementClassView(FormMovementClassPresenter presenter) {
        this.presenter = presenter;

        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");

        movementClassTypeSelect.setLabel("Type");
        movementClassTypeSelect.setItems(MovementClassType.values());
        movementClassTypeSelect.setItemLabelGenerator(mct -> mct != null ? MessageSource.get(mct.toString()) : "");
        movementClassTypeSelect.setWidthFull();

        costCenterComboBox.setItemLabelGenerator(cc -> cc != null ? cc.getName() : "");
        costCenterComboBox.setItems(presenter.findActiveCostCenters());
        costCenterComboBox.setWidthFull();

        nameField.setWidthFull();
        nameField.setRequired(true);

        budgetField.setWidthFull();
        budgetField.setRequired(true);

        var formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        formLayout.add(activeCheckbox);
        formLayout.add(nameField, movementClassTypeSelect);
        formLayout.add(costCenterComboBox, budgetField);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> onSave());

        updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateButton.addClickListener(e -> onUpdate());
        updateButton.setVisible(false);

        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("registration/movement-classes"));

        var toolbar = new HorizontalLayout(saveButton, updateButton, backButton);
        toolbar.setSpacing(true);

        add(formLayout, toolbar);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var idParam = event.getRouteParameters().get("id");
        if (idParam.isPresent()) {
            long id = Long.parseLong(idParam.get());
            presenter.findById(id).ifPresentOrElse(mc -> {
                currentMovementClass = mc;
                editMode = true;
            }, () -> event.forwardTo("registration/movement-classes"));
        } else {
            currentMovementClass = new MovementClass();
            editMode = false;
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        populateForm(currentMovementClass);
        saveButton.setVisible(!editMode);
        updateButton.setVisible(editMode);
    }

    private void populateForm(MovementClass mc) {
        activeCheckbox.setValue(mc.isActive());
        nameField.setValue(mc.getName() != null ? mc.getName() : "");
        movementClassTypeSelect.setValue(mc.getMovementClassType());
        costCenterComboBox.setValue(mc.getCostCenter());
        budgetField.setValue(mc.getBudget() != null ? mc.getBudget() : BigDecimal.ZERO);
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

        if (movementClassTypeSelect.getValue() == null) {
            movementClassTypeSelect.setInvalid(true);
            movementClassTypeSelect.setErrorMessage("Type is required");
            valid = false;
        } else {
            movementClassTypeSelect.setInvalid(false);
        }

        if (costCenterComboBox.getValue() == null) {
            costCenterComboBox.setInvalid(true);
            costCenterComboBox.setErrorMessage("Cost Center is required");
            valid = false;
        } else {
            costCenterComboBox.setInvalid(false);
        }

        if (budgetField.getValue() == null) {
            budgetField.setInvalid(true);
            budgetField.setErrorMessage("Budget is required");
            valid = false;
        } else {
            budgetField.setInvalid(false);
        }

        return valid;
    }

    private MovementClass buildEntity() {
        if (currentMovementClass == null) {
            currentMovementClass = new MovementClass();
        }
        currentMovementClass.setActive(activeCheckbox.getValue());
        currentMovementClass.setName(nameField.getValue());
        currentMovementClass.setMovementClassType(movementClassTypeSelect.getValue());
        currentMovementClass.setCostCenter(costCenterComboBox.getValue());
        currentMovementClass.setBudget(budgetField.getValue());
        return currentMovementClass;
    }

    private void onSave() {
        if (!validateFields()) {
            return;
        }
        try {
            presenter.save(buildEntity());
            currentMovementClass = new MovementClass();
            populateForm(currentMovementClass);
            var notification = Notification.show("Movement class saved successfully.");
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
            var notification = Notification.show("Movement class updated successfully.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(3000);
        } catch (BusinessLogicException ex) {
            var notification = Notification.show(MessageSource.get(ex.getMessage()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        }
    }
}
