package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
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
@Route(value = "registration/financial-periods/detail/:id", layout = MainLayout.class)
@PageTitle("Financial Periods")
public class DetailFinancialPeriodView extends VerticalLayout implements BeforeEnterObserver {

    final Checkbox closedCheckbox = new Checkbox("Closed");
    final TextField identificationField = new TextField("Identification");
    final TextField startField = new TextField("Start");
    final TextField endField = new TextField("End");
    final TextField creditCardGoalField = new TextField("Credit Card Goal");
    final TextField expensesGoalField = new TextField("Expenses Goal");
    final TextField incomesGoalField = new TextField("Income Goal");
    final Button editButton = new Button("Edit");
    final Button deleteButton = new Button("Delete");
    final Button reopenButton = new Button("Reopen");
    final Button statisticsButton = new Button("Statistics");
    final Button backButton = new Button("Back");

    private FinancialPeriod currentPeriod;
    private final DetailFinancialPeriodPresenter presenter;

    public DetailFinancialPeriodView(DetailFinancialPeriodPresenter presenter) {
        this.presenter = presenter;
        setPadding(true); setSpacing(true); setMaxWidth("900px");
        closedCheckbox.setEnabled(false);
        for (var f : new TextField[]{identificationField, startField, endField, creditCardGoalField, expensesGoalField, incomesGoalField}) {
            f.setReadOnly(true); f.setWidthFull();
        }

        var formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));
        formLayout.add(closedCheckbox);
        formLayout.add(identificationField);
        formLayout.add(startField, endField);
        formLayout.add(creditCardGoalField, expensesGoalField);
        formLayout.add(incomesGoalField);

        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e -> { if (currentPeriod != null) UI.getCurrent().navigate(FormFinancialPeriodView.class, new RouteParameters("id", String.valueOf(currentPeriod.getId()))); });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> handleDelete());
        reopenButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        reopenButton.addClickListener(e -> { if (currentPeriod != null) { presenter.reopen(currentPeriod); UI.getCurrent().navigate("registration/financial-periods"); } });
        statisticsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        statisticsButton.addClickListener(e -> { if (currentPeriod != null) UI.getCurrent().navigate(FinancialPeriodStatisticsView.class, new RouteParameters("id", String.valueOf(currentPeriod.getId()))); });
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("registration/financial-periods"));

        add(formLayout, new HorizontalLayout(editButton, statisticsButton, reopenButton, deleteButton, backButton));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("id").map(Long::parseLong).flatMap(presenter::findById)
                .ifPresentOrElse(this::loadPeriod, () -> event.forwardTo("registration/financial-periods"));
    }

    private void loadPeriod(FinancialPeriod fp) {
        currentPeriod = fp;
        closedCheckbox.setValue(fp.isClosed());
        identificationField.setValue(fp.getIdentification() != null ? fp.getIdentification() : "");
        startField.setValue(fp.getStart() != null ? fp.getStart().toString() : "");
        endField.setValue(fp.getEnd() != null ? fp.getEnd().toString() : "");
        creditCardGoalField.setValue(fp.getCreditCardGoal() != null ? fp.getCreditCardGoal().toPlainString() : "");
        expensesGoalField.setValue(fp.getExpensesGoal() != null ? fp.getExpensesGoal().toPlainString() : "");
        incomesGoalField.setValue(fp.getRevenuesGoal() != null ? fp.getRevenuesGoal().toPlainString() : "");
        reopenButton.setVisible(fp.isClosed());
    }

    private void handleDelete() {
        if (currentPeriod == null) return;
        var dialog = new ConfirmDialog();
        dialog.setHeader("Confirm Delete");
        dialog.setText("Delete financial period '" + currentPeriod.getIdentification() + "'?");
        dialog.setCancelable(true); dialog.setCancelText("No"); dialog.setConfirmText("Yes");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(ev -> { presenter.delete(currentPeriod); UI.getCurrent().navigate("registration/financial-periods"); });
        dialog.open();
    }
}
