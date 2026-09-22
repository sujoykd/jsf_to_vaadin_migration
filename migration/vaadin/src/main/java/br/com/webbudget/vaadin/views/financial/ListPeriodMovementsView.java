package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.PeriodMovement;
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
@Route(value = "financial/period-movements", layout = MainLayout.class)
@PageTitle("Period Movements")
public class ListPeriodMovementsView extends VerticalLayout {

    final Grid<PeriodMovement> grid = new Grid<>(PeriodMovement.class, false);
    final TextField filterField = new TextField();

    public ListPeriodMovementsView(ListPeriodMovementsPresenter presenter) {
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        grid.addColumn(movement -> movement.getFinancialPeriod() != null
                ? movement.getFinancialPeriod().getIdentification() : "")
                .setHeader("Financial Period").setSortable(true);
        grid.addColumn(PeriodMovement::getIdentification)
                .setHeader("Identification").setSortable(true);
        grid.addColumn(movement -> movement.getPeriodMovementState() != null
                ? movement.getPeriodMovementState().toString() : "")
                .setHeader("State").setSortable(true);
        grid.addColumn(movement -> movement.getContact() != null
                ? movement.getContact().getName() : "")
                .setHeader("Contact").setSortable(true);
        grid.addColumn(movement -> movement.getDueDate() != null
                ? movement.getDueDate().toString() : "")
                .setHeader("Due Date").setSortable(true);
        grid.addColumn(PeriodMovement::getValue)
                .setHeader("Amount").setSortable(true);
        grid.addColumn(new ComponentRenderer<>(movement -> {
            var payBtn = new Button("Pay");
            payBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            payBtn.addClickListener(e ->
                    UI.getCurrent().navigate(FormPaymentView.class,
                            new RouteParameters("id", String.valueOf(movement.getId()))));

            var detailBtn = new Button("Detail");
            detailBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            detailBtn.addClickListener(e ->
                    UI.getCurrent().navigate(DetailPeriodMovementView.class,
                            new RouteParameters("id", String.valueOf(movement.getId()))));

            var editBtn = new Button("Edit");
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            editBtn.addClickListener(e ->
                    UI.getCurrent().navigate(FormPeriodMovementView.class,
                            new RouteParameters("id", String.valueOf(movement.getId()))));

            var deleteBtn = new Button("Delete");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e ->
                    UI.getCurrent().navigate(DetailPeriodMovementView.class,
                            new RouteParameters("id", String.valueOf(movement.getId()))));

            return new HorizontalLayout(payBtn, detailBtn, editBtn, deleteBtn);
        })).setHeader("Actions");

        grid.setDataProvider(new CallbackDataProvider<>(
                query -> presenter.findAll(
                        filterField.getValue().isEmpty() ? null : filterField.getValue(),
                        query.getOffset(),
                        query.getPageSize()).getContent().stream(),
                query -> presenter.count(
                        filterField.getValue().isEmpty() ? null : filterField.getValue())
        ));
        grid.setSizeFull();

        filterField.setPlaceholder("Filter...");
        filterField.setClearButtonVisible(true);
        filterField.addValueChangeListener(e -> grid.getDataProvider().refreshAll());

        var addBtn = new Button("New", e -> UI.getCurrent().navigate(FormPeriodMovementView.class));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var toolbar = new HorizontalLayout(filterField, addBtn);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.END);
        toolbar.setPadding(true);

        add(toolbar, grid);
        expand(grid);
    }
}
