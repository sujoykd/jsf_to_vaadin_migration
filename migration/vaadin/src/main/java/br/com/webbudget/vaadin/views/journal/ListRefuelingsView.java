package br.com.webbudget.vaadin.views.journal;

import br.com.webbudget.domain.entities.journal.Refueling;
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
@Route(value = "journal/refuelings", layout = MainLayout.class)
@PageTitle("Refuelings")
public class ListRefuelingsView extends VerticalLayout {

    final Grid<Refueling> grid = new Grid<>(Refueling.class, false);
    final TextField filterField = new TextField();

    public ListRefuelingsView(ListRefuelingsPresenter presenter) {
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        grid.addColumn(refueling -> refueling.getEventDate() != null ? refueling.getEventDate().toString() : "")
                .setHeader("Date").setSortable(true);
        grid.addColumn(refueling -> refueling.getVehicle() != null ? refueling.getVehicle().getIdentification() : "")
                .setHeader("Vehicle").setSortable(true);
        grid.addColumn(refueling -> refueling.getLiters() != null ? refueling.getLiters().toPlainString() : "")
                .setHeader("Liters").setSortable(true);
        grid.addColumn(refueling -> refueling.getCost() != null ? refueling.getCost().toPlainString() : "")
                .setHeader("Total").setSortable(true);
        grid.addColumn(refueling -> refueling.getCostPerLiter() != null ? refueling.getCostPerLiter().toPlainString() : "")
                .setHeader("Cost/Liter").setSortable(true);
        grid.addColumn(refueling -> refueling.getAverageConsumption() != null ? refueling.getAverageConsumption().toPlainString() : "")
                .setHeader("Avg. Consumption").setSortable(true);
        grid.addColumn(new ComponentRenderer<>(refueling -> {
            var deleteBtn = new Button("Delete");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e ->
                    UI.getCurrent().navigate(DetailRefuelingView.class,
                            new RouteParameters("id", String.valueOf(refueling.getId()))));

            return new HorizontalLayout(deleteBtn);
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

        var addBtn = new Button("New", e -> UI.getCurrent().navigate(FormRefuelingView.class));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var toolbar = new HorizontalLayout(filterField, addBtn);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.END);
        toolbar.setPadding(true);

        add(toolbar, grid);
        expand(grid);
    }
}
