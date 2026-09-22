package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.MovementClass;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "registration/movement-classes", layout = MainLayout.class)
@PageTitle("Movement Classes")
public class ListMovementClassesView extends VerticalLayout {

    final Grid<MovementClass> grid = new Grid<>(MovementClass.class, false);
    final TextField filterField = new TextField();

    public ListMovementClassesView(ListMovementClassesPresenter presenter) {
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        grid.addColumn(MovementClass::getName).setHeader("Name").setSortable(true);
        grid.addColumn(mc -> mc.getCostCenter() != null ? mc.getCostCenter().getName() : "")
                .setHeader("Cost Center").setSortable(true);
        grid.addColumn(mc -> mc.getBudget() != null ? mc.getBudget().toPlainString() : "")
                .setHeader("Budget").setSortable(true);
        grid.addColumn(new ComponentRenderer<>(mc -> {
            var editBtn = new Button("Edit");
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            editBtn.addClickListener(e ->
                    UI.getCurrent().navigate(FormMovementClassView.class,
                            new RouteParameters("id", String.valueOf(mc.getId()))));

            var deleteBtn = new Button("Delete");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e ->
                    UI.getCurrent().navigate(DetailMovementClassView.class,
                            new RouteParameters("id", String.valueOf(mc.getId()))));

            return new HorizontalLayout(editBtn, deleteBtn);
        })).setHeader("Actions");

        grid.setDataProvider(new CallbackDataProvider<>(
                query -> presenter.findAll(
                        filterField.getValue().isEmpty() ? null : filterField.getValue(),
                        null,
                        query.getOffset(),
                        query.getPageSize()).getContent().stream(),
                query -> presenter.count(
                        filterField.getValue().isEmpty() ? null : filterField.getValue(),
                        null)
        ));
        grid.setSizeFull();

        filterField.setPlaceholder("Filter...");
        filterField.setClearButtonVisible(true);
        filterField.addValueChangeListener(e -> grid.getDataProvider().refreshAll());

        var addBtn = new Button("New", e -> UI.getCurrent().navigate(FormMovementClassView.class));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var toolbar = new HorizontalLayout(filterField, addBtn);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.END);
        toolbar.setPadding(true);

        add(toolbar, grid);
        expand(grid);
    }
}
