package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.CostCenter;
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
@Route(value = "registration/cost-centers", layout = MainLayout.class)
@PageTitle("Cost Centers")
public class ListCostCentersView extends VerticalLayout {

    final Grid<CostCenter> grid = new Grid<>(CostCenter.class, false);
    final TextField filterField = new TextField();

    public ListCostCentersView(ListCostCentersPresenter presenter) {
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        grid.addColumn(CostCenter::getName).setHeader("Name").setSortable(true);
        grid.addColumn(cc -> cc.getRevenuesBudget() != null ? cc.getRevenuesBudget().toPlainString() : "")
                .setHeader("Inc. Budget").setSortable(true);
        grid.addColumn(cc -> cc.getExpensesBudget() != null ? cc.getExpensesBudget().toPlainString() : "")
                .setHeader("Exp. Budget").setSortable(true);
        grid.addColumn(new ComponentRenderer<>(cc -> {
            var editBtn = new Button("Edit");
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            editBtn.addClickListener(e ->
                    UI.getCurrent().navigate(FormCostCenterView.class,
                            new RouteParameters("id", String.valueOf(cc.getId()))));

            var deleteBtn = new Button("Delete");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e ->
                    UI.getCurrent().navigate(DetailCostCenterView.class,
                            new RouteParameters("id", String.valueOf(cc.getId()))));

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

        var addBtn = new Button("New", e -> UI.getCurrent().navigate(FormCostCenterView.class));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var toolbar = new HorizontalLayout(filterField, addBtn);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.END);
        toolbar.setPadding(true);

        add(toolbar, grid);
        expand(grid);
    }
}
