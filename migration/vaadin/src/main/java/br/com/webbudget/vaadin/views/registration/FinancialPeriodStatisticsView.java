package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.entities.view.UseByCostCenter;
import br.com.webbudget.domain.entities.view.UseByMovementClass;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "registration/financial-periods/statistics/:id", layout = MainLayout.class)
@PageTitle("Financial Period Statistics")
public class FinancialPeriodStatisticsView extends VerticalLayout implements BeforeEnterObserver {

    final TextField identificationField = new TextField("Identification");
    final TextField startDateField = new TextField("Start Date");
    final TextField endDateField = new TextField("End Date");

    final Grid<UseByCostCenter> expensesByCostCenterGrid = new Grid<>(UseByCostCenter.class, false);
    final Grid<UseByCostCenter> revenuesByCostCenterGrid = new Grid<>(UseByCostCenter.class, false);
    final Grid<UseByMovementClass> expensesByMovementClassGrid = new Grid<>(UseByMovementClass.class, false);
    final Grid<UseByMovementClass> revenuesByMovementClassGrid = new Grid<>(UseByMovementClass.class, false);

    final Button refreshButton = new Button("Refresh");
    final Button backButton = new Button("Back");

    private final FinancialPeriodStatisticsPresenter presenter;
    private long currentPeriodId;

    public FinancialPeriodStatisticsView(FinancialPeriodStatisticsPresenter presenter) {
        this.presenter = presenter;

        setPadding(true);
        setSpacing(true);
        setMaxWidth("1000px");

        identificationField.setReadOnly(true);
        identificationField.setWidthFull();
        startDateField.setReadOnly(true);
        startDateField.setWidthFull();
        endDateField.setReadOnly(true);
        endDateField.setWidthFull();

        var periodInfo = new HorizontalLayout(identificationField, startDateField, endDateField);
        periodInfo.setWidthFull();

        expensesByCostCenterGrid.addColumn(UseByCostCenter::getCostCenter).setHeader("Cost Center");
        expensesByCostCenterGrid.addColumn(u -> u.getValue() != null ? u.getValue().toPlainString() : "").setHeader("Value");
        expensesByCostCenterGrid.setHeight("200px");

        revenuesByCostCenterGrid.addColumn(UseByCostCenter::getCostCenter).setHeader("Cost Center");
        revenuesByCostCenterGrid.addColumn(u -> u.getValue() != null ? u.getValue().toPlainString() : "").setHeader("Value");
        revenuesByCostCenterGrid.setHeight("200px");

        expensesByMovementClassGrid.addColumn(UseByMovementClass::getCostCenter).setHeader("Cost Center");
        expensesByMovementClassGrid.addColumn(UseByMovementClass::getMovementClass).setHeader("Movement Class");
        expensesByMovementClassGrid.addColumn(u -> u.getValue() != null ? u.getValue().toPlainString() : "").setHeader("Value");
        expensesByMovementClassGrid.setHeight("200px");

        revenuesByMovementClassGrid.addColumn(UseByMovementClass::getCostCenter).setHeader("Cost Center");
        revenuesByMovementClassGrid.addColumn(UseByMovementClass::getMovementClass).setHeader("Movement Class");
        revenuesByMovementClassGrid.addColumn(u -> u.getValue() != null ? u.getValue().toPlainString() : "").setHeader("Value");
        revenuesByMovementClassGrid.setHeight("200px");

        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addClickListener(e -> loadData(currentPeriodId));

        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("registration/financial-periods"));

        add(periodInfo,
                new H3("Expenses by Cost Center"), expensesByCostCenterGrid,
                new H3("Revenues by Cost Center"), revenuesByCostCenterGrid,
                new H3("Expenses by Movement Class"), expensesByMovementClassGrid,
                new H3("Revenues by Movement Class"), revenuesByMovementClassGrid,
                new HorizontalLayout(refreshButton, backButton));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("id")
                .map(Long::parseLong)
                .flatMap(presenter::findById)
                .ifPresentOrElse(this::loadPeriod,
                        () -> event.forwardTo("registration/financial-periods"));
    }

    private void loadPeriod(FinancialPeriod period) {
        currentPeriodId = period.getId();
        identificationField.setValue(period.getIdentification() != null ? period.getIdentification() : "");
        startDateField.setValue(period.getStart() != null ? period.getStart().toString() : "");
        endDateField.setValue(period.getEnd() != null ? period.getEnd().toString() : "");
        loadData(currentPeriodId);
    }

    private void loadData(long periodId) {
        expensesByCostCenterGrid.setItems(presenter.findExpensesByCostCenter(periodId));
        revenuesByCostCenterGrid.setItems(presenter.findRevenuesByCostCenter(periodId));
        expensesByMovementClassGrid.setItems(presenter.findExpensesByMovementClass(periodId));
        revenuesByMovementClassGrid.setItems(presenter.findRevenuesByMovementClass(periodId));
    }
}
