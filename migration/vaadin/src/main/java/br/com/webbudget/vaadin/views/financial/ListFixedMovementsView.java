package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.FixedMovement;
import br.com.webbudget.domain.entities.financial.FixedMovementState;
import br.com.webbudget.infrastructure.i18n.MessageSource;
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
@Route(value = "financial/fixed-movements", layout = MainLayout.class)
@PageTitle("Fixed Movements")
public class ListFixedMovementsView extends VerticalLayout {

    final Grid<FixedMovement> grid = new Grid<>(FixedMovement.class, false);
    final TextField filterField = new TextField();

    public ListFixedMovementsView(ListFixedMovementsPresenter presenter) {
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        grid.addColumn(FixedMovement::getIdentification).setHeader("Identification").setSortable(true);
        grid.addColumn(movement -> movement.isUndetermined() ? "Undetermined" : movement.getQuoteState())
                .setHeader("Installments").setSortable(false);
        grid.addColumn(FixedMovement::getValue).setHeader("Value").setSortable(true);
        grid.addColumn(movement -> movement.getFixedMovementState() != null
                ? MessageSource.get(movement.getFixedMovementState().toString()) : "")
                .setHeader("Status").setSortable(true);
        grid.addColumn(new ComponentRenderer<>(movement -> {
            var editBtn = new Button("Edit");
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            editBtn.addClickListener(e ->
                    UI.getCurrent().navigate(FormFixedMovementView.class,
                            new RouteParameters("id", String.valueOf(movement.getId()))));

            var deleteBtn = new Button("Delete");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e ->
                    UI.getCurrent().navigate(DetailFixedMovementView.class,
                            new RouteParameters("id", String.valueOf(movement.getId()))));

            var detailsBtn = new Button("Details");
            detailsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            detailsBtn.addClickListener(e ->
                    UI.getCurrent().navigate(DetailFixedMovementView.class,
                            new RouteParameters("id", String.valueOf(movement.getId()))));

            return new HorizontalLayout(editBtn, deleteBtn, detailsBtn);
        })).setHeader("Actions");

        grid.setDataProvider(new CallbackDataProvider<>(
                query -> presenter.findAll(
                        filterField.getValue().isEmpty() ? null : filterField.getValue(),
                        FixedMovementState.ACTIVE,
                        query.getOffset(),
                        query.getPageSize()).getContent().stream(),
                query -> presenter.count(
                        filterField.getValue().isEmpty() ? null : filterField.getValue(),
                        FixedMovementState.ACTIVE)
        ));
        grid.setSizeFull();

        filterField.setPlaceholder("Filter...");
        filterField.setClearButtonVisible(true);
        filterField.addValueChangeListener(e -> grid.getDataProvider().refreshAll());

        var addBtn = new Button("New", e -> UI.getCurrent().navigate(FormFixedMovementView.class));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var toolbar = new HorizontalLayout(filterField, addBtn);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.END);
        toolbar.setPadding(true);

        add(toolbar, grid);
        expand(grid);
    }
}
