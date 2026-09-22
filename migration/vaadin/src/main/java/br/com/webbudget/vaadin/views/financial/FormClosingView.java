package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.Closing;
import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;

@PermitAll
@Route(value = "financial/closing", layout = MainLayout.class)
@PageTitle("Closing")
public class FormClosingView extends VerticalLayout {

    final ComboBox<FinancialPeriod> periodComboBox = new ComboBox<>("Financial Period");

    private final VerticalLayout summaryPanel = new VerticalLayout();

    private final Paragraph revenuesLabel = new Paragraph();
    private final Paragraph expensesLabel = new Paragraph();
    private final Paragraph creditCardExpensesLabel = new Paragraph();
    private final Paragraph debitCardExpensesLabel = new Paragraph();
    private final Paragraph cashExpensesLabel = new Paragraph();
    private final Paragraph balanceLabel = new Paragraph();

    public FormClosingView(FormClosingPresenter presenter) {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("800px");

        periodComboBox.setItemLabelGenerator(FinancialPeriod::getIdentification);
        periodComboBox.setItems(presenter.loadOpenPeriods());
        periodComboBox.setPlaceholder("Select an open period...");
        periodComboBox.setWidthFull();

        summaryPanel.setVisible(false);
        summaryPanel.setPadding(true);
        summaryPanel.setSpacing(false);
        summaryPanel.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background-color", "var(--lumo-contrast-5pct)");

        summaryPanel.add(new H3("Simulation Result"));
        summaryPanel.add(revenuesLabel);
        summaryPanel.add(expensesLabel);
        summaryPanel.add(creditCardExpensesLabel);
        summaryPanel.add(debitCardExpensesLabel);
        summaryPanel.add(cashExpensesLabel);
        summaryPanel.add(balanceLabel);

        var simulateBtn = new Button("Simulate", e -> {
            var selected = periodComboBox.getValue();
            if (selected == null) {
                var notification = Notification.show("Please select a financial period.");
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                notification.setDuration(3000);
                return;
            }
            Closing result = presenter.simulate(selected);
            showSummary(result);
        });
        simulateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var closeBtn = new Button("Close Period", e -> {
            var selected = periodComboBox.getValue();
            if (selected == null) {
                var notification = Notification.show("Please select a financial period.");
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                notification.setDuration(3000);
                return;
            }
            var dialog = new ConfirmDialog();
            dialog.setHeader("Confirm Closing");
            dialog.setText("Are you sure you want to close the period '" + selected.getIdentification() + "'? This action cannot be undone.");
            dialog.setCancelable(true);
            dialog.setCancelText("Cancel");
            dialog.setConfirmText("Close Period");
            dialog.setConfirmButtonTheme("error primary");
            dialog.addConfirmListener(event -> {
                presenter.close(selected);
                var notification = Notification.show("Period closed successfully.");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setDuration(3000);
                UI.getCurrent().navigate("registration/financial-periods");
            });
            dialog.open();
        });
        closeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        var cancelBtn = new Button("Cancel", e -> UI.getCurrent().navigate("registration/financial-periods"));
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        var toolbar = new HorizontalLayout(simulateBtn, closeBtn, cancelBtn);
        toolbar.setSpacing(true);

        add(periodComboBox, toolbar, summaryPanel);
    }

    private void showSummary(Closing closing) {
        revenuesLabel.setText("Revenues: " + formatAmount(closing.getRevenues()));
        expensesLabel.setText("Expenses: " + formatAmount(closing.getExpenses()));
        creditCardExpensesLabel.setText("Credit Card Expenses: " + formatAmount(closing.getCreditCardExpenses()));
        debitCardExpensesLabel.setText("Debit Card Expenses: " + formatAmount(closing.getDebitCardExpenses()));
        cashExpensesLabel.setText("Cash Expenses: " + formatAmount(closing.getCashExpenses()));
        balanceLabel.setText("Balance: " + formatAmount(closing.getBalance()));
        summaryPanel.setVisible(true);
    }

    private String formatAmount(BigDecimal value) {
        return value != null ? value.toPlainString() : "0.00";
    }
}
