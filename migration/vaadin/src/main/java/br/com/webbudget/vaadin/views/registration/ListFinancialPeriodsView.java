package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.vaadin.layout.MainLayout;
import br.com.webbudget.vaadin.views.financial.FormClosingView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
@Route(value = "registration/financial-periods", layout = MainLayout.class)
@PageTitle("Financial Periods")
public class ListFinancialPeriodsView extends VerticalLayout {

    final Grid<FinancialPeriod> grid = new Grid<>(FinancialPeriod.class, false);
    final TextField filterField = new TextField();

    public ListFinancialPeriodsView(ListFinancialPeriodsPresenter presenter) {
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        grid.addColumn(FinancialPeriod::getIdentification)
                .setHeader("Identification").setSortable(true);
        grid.addColumn(fp -> fp.getStart() != null ? fp.getStart().toString() : "")
                .setHeader("Start").setSortable(true);
        grid.addColumn(fp -> fp.getEnd() != null ? fp.getEnd().toString() : "")
                .setHeader("End").setSortable(true);
        grid.addColumn(fp -> fp.getClosing() != null && fp.getClosing().getClosingDate() != null
                ? fp.getClosing().getClosingDate().toString() : "")
                .setHeader("Closing Date").setSortable(false);
        grid.addColumn(fp -> fp.isClosed() ? "Closed" : "Open")
                .setHeader("Status").setSortable(true);
        grid.addColumn(new ComponentRenderer<>(fp -> {
            var deleteBtn = new Button("Delete");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e ->
                    UI.getCurrent().navigate(DetailFinancialPeriodView.class,
                            new RouteParameters("id", String.valueOf(fp.getId()))));

            var statisticsBtn = new Button("Statistics");
            statisticsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            statisticsBtn.addClickListener(e ->
                    UI.getCurrent().navigate(FinancialPeriodStatisticsView.class,
                            new RouteParameters("id", String.valueOf(fp.getId()))));

            var closeBtn = new Button("Close Period");
            closeBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            closeBtn.addClickListener(e ->
                    UI.getCurrent().navigate(FormClosingView.class,
                            new RouteParameters("id", String.valueOf(fp.getId()))));

            var actions = new HorizontalLayout(deleteBtn, statisticsBtn, closeBtn);

            if (fp.isClosed()) {
                var reopenBtn = new Button("Reopen");
                reopenBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_WARNING);
                reopenBtn.addClickListener(e -> {
                    var dialog = new ConfirmDialog();
                    dialog.setHeader("Reopen period");
                    dialog.setText("Are you sure you want to reopen the period \""
                            + fp.getIdentification() + "\"?");
                    dialog.setCancelable(true);
                    dialog.setConfirmText("Reopen");
                    dialog.addConfirmListener(ev -> {
                        presenter.reopen(fp);
                        grid.getDataProvider().refreshAll();
                    });
                    dialog.open();
                });
                actions.add(reopenBtn);
            }

            return actions;
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

        var addBtn = new Button("New", e -> UI.getCurrent().navigate(FormFinancialPeriodView.class));
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var toolbar = new HorizontalLayout(filterField, addBtn);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.END);
        toolbar.setPadding(true);

        add(toolbar, grid);
        expand(grid);
    }
}
