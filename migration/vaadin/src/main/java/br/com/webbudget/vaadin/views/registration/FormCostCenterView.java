package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.application.components.dto.Color;
import br.com.webbudget.domain.entities.registration.CostCenter;
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
@Route(value = "registration/cost-centers/form/:id?", layout = MainLayout.class)
@PageTitle("Cost Centers")
public class FormCostCenterView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    final Checkbox activeCheckbox = new Checkbox("Active");
    final TextField nameField = new TextField("Name");
    final ComboBox<CostCenter> parentComboBox = new ComboBox<>("Parent Cost Center");
    final BigDecimalField incomeBudgetField = new BigDecimalField("Income Budget");
    final BigDecimalField expenseBudgetField = new BigDecimalField("Expense Budget");
    final TextField colorField = new TextField("Color");
    final TextArea descriptionArea = new TextArea("Description");

    Button saveButton = new Button("Save");
    Button updateButton = new Button("Update");
    Button backButton = new Button("Back");

    private boolean editMode = false;
    private CostCenter currentCostCenter;

    private final FormCostCenterPresenter presenter;

    public FormCostCenterView(FormCostCenterPresenter presenter) {
        this.presenter = presenter;

        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");

        nameField.setWidthFull();
        nameField.setRequired(true);

        parentComboBox.setWidthFull();
        parentComboBox.setItemLabelGenerator(cc -> cc != null ? cc.getName() : "");
        parentComboBox.setItems(presenter.findAllActive());

        incomeBudgetField.setWidthFull();
        incomeBudgetField.setRequired(true);

        expenseBudgetField.setWidthFull();
        expenseBudgetField.setRequired(true);

        colorField.setWidthFull();

        descriptionArea.setWidthFull();
        descriptionArea.setMinHeight("80px");

        var formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        formLayout.add(activeCheckbox, nameField);
        formLayout.add(parentComboBox, colorField);
        formLayout.add(incomeBudgetField, expenseBudgetField);
        formLayout.add(descriptionArea, 2);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> onSave());

        updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateButton.addClickListener(e -> onUpdate());
        updateButton.setVisible(false);

        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("registration/cost-centers"));

        var toolbar = new HorizontalLayout(saveButton, updateButton, backButton);
        toolbar.setSpacing(true);

        add(formLayout, toolbar);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var idParam = event.getRouteParameters().get("id");
        if (idParam.isPresent()) {
            long id = Long.parseLong(idParam.get());
            presenter.findById(id).ifPresentOrElse(cc -> {
                currentCostCenter = cc;
                editMode = true;
            }, () -> event.forwardTo("registration/cost-centers"));
        } else {
            currentCostCenter = new CostCenter();
            editMode = false;
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        populateForm(currentCostCenter);
        saveButton.setVisible(!editMode);
        updateButton.setVisible(editMode);
    }

    private void populateForm(CostCenter cc) {
        activeCheckbox.setValue(cc.isActive());
        nameField.setValue(cc.getName() != null ? cc.getName() : "");
        parentComboBox.setValue(cc.getParent());
        incomeBudgetField.setValue(cc.getRevenuesBudget() != null ? cc.getRevenuesBudget() : BigDecimal.ZERO);
        expenseBudgetField.setValue(cc.getExpensesBudget() != null ? cc.getExpensesBudget() : BigDecimal.ZERO);
        colorField.setValue(cc.getColor() != null ? cc.getColor().toString() : "");
        descriptionArea.setValue(cc.getDescription() != null ? cc.getDescription() : "");
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
        if (incomeBudgetField.getValue() == null) {
            incomeBudgetField.setInvalid(true);
            incomeBudgetField.setErrorMessage("Income budget is required");
            valid = false;
        } else {
            incomeBudgetField.setInvalid(false);
        }
        if (expenseBudgetField.getValue() == null) {
            expenseBudgetField.setInvalid(true);
            expenseBudgetField.setErrorMessage("Expense budget is required");
            valid = false;
        } else {
            expenseBudgetField.setInvalid(false);
        }
        return valid;
    }

    private CostCenter buildEntity() {
        if (currentCostCenter == null) {
            currentCostCenter = new CostCenter();
        }
        currentCostCenter.setActive(activeCheckbox.getValue());
        currentCostCenter.setName(nameField.getValue());
        currentCostCenter.setParent(parentComboBox.getValue());
        currentCostCenter.setRevenuesBudget(incomeBudgetField.getValue() != null ? incomeBudgetField.getValue() : BigDecimal.ZERO);
        currentCostCenter.setExpensesBudget(expenseBudgetField.getValue() != null ? expenseBudgetField.getValue() : BigDecimal.ZERO);
        String colorText = colorField.getValue();
        currentCostCenter.setColor(colorText != null && !colorText.isBlank() ? Color.parse(colorText) : Color.randomize());
        currentCostCenter.setDescription(descriptionArea.getValue());
        return currentCostCenter;
    }

    private void onSave() {
        if (!validateFields()) {
            return;
        }
        try {
            presenter.save(buildEntity());
            currentCostCenter = new CostCenter();
            populateForm(currentCostCenter);
            var notification = Notification.show("Cost center saved successfully.");
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
            var notification = Notification.show("Cost center updated successfully.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(3000);
        } catch (BusinessLogicException ex) {
            var notification = Notification.show(MessageSource.get(ex.getMessage()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        }
    }
}
