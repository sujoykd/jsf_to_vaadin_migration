package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.MovementClass;
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
@Route(value = "registration/movement-classes/detail/:id", layout = MainLayout.class)
@PageTitle("Movement Classes")
public class DetailMovementClassView extends VerticalLayout implements BeforeEnterObserver {

    final Checkbox activeCheckbox = new Checkbox("Active");
    final TextField nameField = new TextField("Name");
    final TextField typeField = new TextField("Movement Class Type");
    final TextField costCenterField = new TextField("Cost Center");
    final TextField budgetField = new TextField("Budget");
    final Button editButton = new Button("Edit");
    final Button deleteButton = new Button("Delete");
    final Button backButton = new Button("Back");

    private MovementClass currentMovementClass;
    private final DetailMovementClassPresenter presenter;

    public DetailMovementClassView(DetailMovementClassPresenter presenter) {
        this.presenter = presenter;
        setPadding(true); setSpacing(true); setMaxWidth("900px");
        activeCheckbox.setEnabled(false);
        for (var f : new TextField[]{nameField, typeField, costCenterField, budgetField}) {
            f.setReadOnly(true); f.setWidthFull();
        }

        var formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));
        formLayout.add(activeCheckbox);
        formLayout.add(nameField, typeField);
        formLayout.add(costCenterField, budgetField);

        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e -> { if (currentMovementClass != null) UI.getCurrent().navigate(FormMovementClassView.class, new RouteParameters("id", String.valueOf(currentMovementClass.getId()))); });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> handleDelete());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("registration/movement-classes"));

        add(formLayout, new HorizontalLayout(editButton, deleteButton, backButton));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("id").map(Long::parseLong).flatMap(presenter::findById)
                .ifPresentOrElse(this::loadMovementClass, () -> event.forwardTo("registration/movement-classes"));
    }

    private void loadMovementClass(MovementClass mc) {
        currentMovementClass = mc;
        activeCheckbox.setValue(mc.isActive());
        nameField.setValue(mc.getName() != null ? mc.getName() : "");
        typeField.setValue(mc.getMovementClassType() != null ? mc.getMovementClassType().name() : "");
        costCenterField.setValue(mc.getCostCenter() != null && mc.getCostCenter().getName() != null ? mc.getCostCenter().getName() : "");
        budgetField.setValue(mc.getBudget() != null ? mc.getBudget().toPlainString() : "");
    }

    private void handleDelete() {
        if (currentMovementClass == null) return;
        var dialog = new ConfirmDialog();
        dialog.setHeader("Confirm Delete");
        dialog.setText("Delete movement class '" + currentMovementClass.getName() + "'?");
        dialog.setCancelable(true); dialog.setCancelText("No"); dialog.setConfirmText("Yes");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(ev -> { presenter.delete(currentMovementClass); UI.getCurrent().navigate("registration/movement-classes"); });
        dialog.open();
    }
}
