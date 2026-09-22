package br.com.webbudget.vaadin.views.configuration;

import br.com.webbudget.domain.entities.configuration.Group;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
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
@Route(value = "configuration/groups", layout = MainLayout.class)
@PageTitle("Groups")
public class ListGroupsView extends VerticalLayout {

    final Grid<Group> grid = new Grid<>(Group.class, false);
    final TextField filterField = new TextField();

    public ListGroupsView(ListGroupsPresenter presenter) {
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        grid.addColumn(Group::getName).setHeader("Name").setSortable(true);
        grid.addColumn(new ComponentRenderer<>(item -> {
            var badge = new Span(item.isActive() ? "Active" : "Inactive");
            badge.getElement().getThemeList().add(item.isActive() ? "badge success" : "badge error");
            return badge;
        })).setHeader("Status");
        grid.addColumn(new ComponentRenderer<>(item -> {
            var editBtn = new Button("Edit");
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            editBtn.addClickListener(e ->
                UI.getCurrent().navigate(FormGroupView.class, new RouteParameters("id", String.valueOf(item.getId()))));
            var deleteBtn = new Button("Delete");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e ->
                UI.getCurrent().navigate(DetailGroupView.class, new RouteParameters("id", String.valueOf(item.getId()))));
            return new HorizontalLayout(editBtn, deleteBtn);
        })).setHeader("Actions");

        grid.setDataProvider(new CallbackDataProvider<>(
            query -> presenter.findAll(
                filterField.getValue().isEmpty() ? null : filterField.getValue(),
                null,
                query.getOffset(),
                query.getPageSize()
            ).getContent().stream(),
            query -> presenter.count(
                filterField.getValue().isEmpty() ? null : filterField.getValue(),
                null
            )
        ));
        grid.setSizeFull();

        filterField.setPlaceholder("Filter...");
        filterField.setClearButtonVisible(true);
        filterField.addValueChangeListener(e -> grid.getDataProvider().refreshAll());

        var addBtn = new Button("New", e -> UI.getCurrent().navigate(FormGroupView.class));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        var toolbar = new HorizontalLayout(filterField, addBtn);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.END);
        toolbar.setPadding(true);

        add(toolbar, grid);
        expand(grid);
    }
}
