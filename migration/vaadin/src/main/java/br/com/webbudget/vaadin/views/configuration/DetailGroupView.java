package br.com.webbudget.vaadin.views.configuration;

import br.com.webbudget.domain.entities.configuration.Grant;
import br.com.webbudget.domain.entities.configuration.Group;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
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
@PageTitle("Groups")
@Route(value = "configuration/groups/:id", layout = MainLayout.class)
public class DetailGroupView extends VerticalLayout implements BeforeEnterObserver {

    final TextField nameField = new TextField("Name");
    final Checkbox activeCheckbox = new Checkbox("Active");
    final TextField parentField = new TextField("Parent Group");
    final Grid<Grant> grantsGrid = new Grid<>(Grant.class, false);

    private final DetailGroupPresenter presenter;
    private Group currentGroup;

    public DetailGroupView(DetailGroupPresenter presenter) {
        this.presenter = presenter;

        setPadding(true);
        setSpacing(true);
        setMaxWidth("800px");

        nameField.setReadOnly(true);
        activeCheckbox.setReadOnly(true);
        parentField.setReadOnly(true);

        var formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.add(nameField, activeCheckbox, parentField);
        formLayout.setColspan(parentField, 2);

        grantsGrid.addColumn(grant -> grant.getAuthorization().getFunctionality())
                .setHeader("Functionality");
        grantsGrid.addColumn(grant -> grant.getAuthorization().getPermission())
                .setHeader("Permission");
        grantsGrid.setHeight("200px");

        var grantsSection = new Div();
        grantsSection.setWidthFull();
        grantsSection.add(grantsGrid);

        var editBtn = new Button("Edit");
        editBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editBtn.addClickListener(e -> {
            if (currentGroup != null) {
                UI.getCurrent().navigate(FormGroupView.class,
                        new RouteParameters("id", String.valueOf(currentGroup.getId())));
            }
        });

        var deleteBtn = new Button("Delete");
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteBtn.addClickListener(e -> {
            var dialog = new ConfirmDialog();
            dialog.setHeader("Confirm deletion");
            dialog.setText("Are you sure you want to delete this group? This action cannot be undone.");
            dialog.setConfirmText("Delete");
            dialog.setConfirmButtonTheme("error primary");
            dialog.setCancelText("Cancel");
            dialog.setCancelable(true);
            dialog.addConfirmListener(event -> {
                presenter.delete(currentGroup);
                UI.getCurrent().navigate("configuration/groups");
            });
            dialog.open();
        });

        var backBtn = new Button("Back");
        backBtn.addClickListener(e -> UI.getCurrent().navigate("configuration/groups"));

        var toolbar = new HorizontalLayout(editBtn, deleteBtn, backBtn);
        toolbar.setSpacing(true);

        add(formLayout, grantsSection, toolbar);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var idParam = event.getRouteParameters().get("id");
        if (idParam.isEmpty()) {
            event.forwardTo("configuration/groups");
            return;
        }

        try {
            long id = Long.parseLong(idParam.get());
            var optGroup = presenter.findById(id);
            if (optGroup.isEmpty()) {
                event.forwardTo("configuration/groups");
                return;
            }

            currentGroup = optGroup.get();
            nameField.setValue(currentGroup.getName() != null ? currentGroup.getName() : "");
            activeCheckbox.setValue(currentGroup.isActive());
            parentField.setValue(currentGroup.getParent() != null
                    ? currentGroup.getParent().getName()
                    : "");
            grantsGrid.setItems(currentGroup.getGrants());
        } catch (NumberFormatException e) {
            event.forwardTo("configuration/groups");
        }
    }
}
