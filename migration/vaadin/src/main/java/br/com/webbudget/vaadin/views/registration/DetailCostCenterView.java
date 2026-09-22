package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "registration/cost-centers/detail/:id", layout = MainLayout.class)
@PageTitle("Cost Centers")
public class DetailCostCenterView extends VerticalLayout implements BeforeEnterObserver {

    final Checkbox activeCheckbox = new Checkbox("Active");
    final TextField nameField = new TextField("Name");
    final TextField parentField = new TextField("Parent Cost Center");
    final TextField incomeBudgetField = new TextField("Income Budget");
    final TextField expenseBudgetField = new TextField("Expense Budget");
    final TextField colorField = new TextField("Color");
    final TextArea descriptionArea = new TextArea("Description");
    final Button editButton = new Button("Edit");
    final Button deleteButton = new Button("Delete");
    final Button backButton = new Button("Back");

    private CostCenter currentCostCenter;
    private final DetailCostCenterPresenter presenter;

    public DetailCostCenterView(DetailCostCenterPresenter presenter) {
        this.presenter = presenter;
        setPadding(true); setSpacing(true); setMaxWidth("900px");
        activeCheckbox.setEnabled(false);
        for (var f : new TextField[]{nameField, parentField, incomeBudgetField, expenseBudgetField, colorField}) {
            f.setReadOnly(true); f.setWidthFull();
        }
        descriptionArea.setReadOnly(true); descriptionArea.setWidthFull();

        var formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));
        formLayout.add(activeCheckbox);
        formLayout.add(nameField, parentField);
        formLayout.add(incomeBudgetField, expenseBudgetField);
        formLayout.add(colorField);
        formLayout.add(descriptionArea, 2);

        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e -> { if (currentCostCenter != null) UI.getCurrent().navigate(FormCostCenterView.class, new RouteParameters("id", String.valueOf(currentCostCenter.getId()))); });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> handleDelete());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("registration/cost-centers"));

        add(formLayout, new HorizontalLayout(editButton, deleteButton, backButton));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("id").map(Long::parseLong).flatMap(presenter::findById)
                .ifPresentOrElse(this::loadCostCenter, () -> event.forwardTo("registration/cost-centers"));
    }

    private void loadCostCenter(CostCenter cc) {
        currentCostCenter = cc;
        activeCheckbox.setValue(cc.isActive());
        nameField.setValue(cc.getName() != null ? cc.getName() : "");
        parentField.setValue(cc.getParent() != null && cc.getParent().getName() != null ? cc.getParent().getName() : "");
        incomeBudgetField.setValue(cc.getRevenuesBudget() != null ? cc.getRevenuesBudget().toPlainString() : "");
        expenseBudgetField.setValue(cc.getExpensesBudget() != null ? cc.getExpensesBudget().toPlainString() : "");
        colorField.setValue(cc.getColor() != null ? cc.getColor().toString() : "");
        descriptionArea.setValue(cc.getDescription() != null ? cc.getDescription() : "");
    }

    private void handleDelete() {
        if (currentCostCenter == null) return;
        var dialog = new ConfirmDialog();
        dialog.setHeader("Confirm Delete");
        dialog.setText("Delete cost center '" + currentCostCenter.getName() + "'?");
        dialog.setCancelable(true); dialog.setCancelText("No"); dialog.setConfirmText("Yes");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(ev -> { presenter.delete(currentCostCenter); UI.getCurrent().navigate("registration/cost-centers"); });
        dialog.open();
    }
}
