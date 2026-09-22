package br.com.webbudget.vaadin.views;

import br.com.webbudget.domain.calculators.PeriodMovementCalculator;
import br.com.webbudget.domain.repositories.registration.FinancialPeriodRepository;
import br.com.webbudget.domain.repositories.registration.WalletRepository;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@PermitAll
@Route(value = "dashboard", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("Dashboard")
public class DashboardView extends VerticalLayout {

    final Span openPeriodsCount = new Span();
    final Span walletsCount = new Span();
    final Span expensesValue = new Span();
    final Span revenuesValue = new Span();
    final Span creditCardExpenses = new Span();

    public DashboardView(FinancialPeriodRepository financialPeriodRepository,
                         WalletRepository walletRepository,
                         PeriodMovementCalculator periodMovementCalculator) {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Dashboard"));

        periodMovementCalculator.load();

        int openPeriods = financialPeriodRepository.findByClosedOrderByIdentificationAsc(false).size();
        long wallets = walletRepository.count();

        BigDecimal expenses = periodMovementCalculator.getExpensesValue();
        BigDecimal revenues = periodMovementCalculator.getRevenuesValue();
        BigDecimal ccExpenses = periodMovementCalculator.getCreditCardExpensesValue();

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

        openPeriodsCount.setText(String.valueOf(openPeriods));
        walletsCount.setText(String.valueOf(wallets));
        expensesValue.setText(currencyFormat.format(expenses));
        revenuesValue.setText(currencyFormat.format(revenues));
        creditCardExpenses.setText(currencyFormat.format(ccExpenses));

        var periodsCard = buildStatCard("Open Periods", openPeriodsCount);
        var walletsCard = buildStatCard("Wallets", walletsCount);
        var expensesCard = buildStatCard("Expenses (Current)", expensesValue);
        var revenuesCard = buildStatCard("Revenues (Current)", revenuesValue);
        var ccExpensesCard = buildStatCard("Credit Card Expenses", creditCardExpenses);

        var topRow = new HorizontalLayout(periodsCard, walletsCard);
        topRow.setWidthFull();
        topRow.setSpacing(true);

        var bottomRow = new HorizontalLayout(expensesCard, revenuesCard, ccExpensesCard);
        bottomRow.setWidthFull();
        bottomRow.setSpacing(true);

        add(topRow, bottomRow);
    }

    private VerticalLayout buildStatCard(String title, Span valueSpan) {
        var card = new VerticalLayout();
        card.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("background-color", "var(--lumo-base-color)");
        card.setSpacing(false);
        card.setPadding(true);

        var titleLabel = new H3(title);
        titleLabel.getStyle().set("margin", "0 0 var(--lumo-space-xs) 0").set("font-size", "var(--lumo-font-size-s)");

        valueSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-text-color)");

        card.add(titleLabel, valueSpan);
        return card;
    }
}
