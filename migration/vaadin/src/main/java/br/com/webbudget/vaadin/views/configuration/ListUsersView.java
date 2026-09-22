package br.com.webbudget.vaadin.views.configuration;

import br.com.webbudget.domain.entities.configuration.User;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
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
@Route(value = "configuration/users", layout = MainLayout.class)
@PageTitle("Users")
public class ListUsersView extends VerticalLayout {

    final Grid<User> grid = new Grid<>(User.class, false);
    final TextField filterField = new TextField();

    public ListUsersView(ListUsersPresenter presenter) {
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        grid.addColumn(User::getName).setHeader("Name").setSortable(true);
        grid.addColumn(User::getUsername).setHeader("Username").setSortable(true);
        grid.addColumn(new ComponentRenderer<>(item -> {
            var editBtn = new Button("Edit");
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            editBtn.addClickListener(e ->
                UI.getCurrent().navigate(FormUserView.class, new RouteParameters("id", String.valueOf(item.getId()))));
            var deleteBtn = new Button("Delete");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e ->
                UI.getCurrent().navigate(DetailUserView.class, new RouteParameters("id", String.valueOf(item.getId()))));
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

        var addBtn = new Button("New", e -> UI.getCurrent().navigate(FormUserView.class));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        var toolbar = new HorizontalLayout(filterField, addBtn);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.END);
        toolbar.setPadding(true);

        add(toolbar, grid);
        expand(grid);
    }
}
