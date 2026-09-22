package br.com.webbudget.vaadin.layout;

import br.com.webbudget.vaadin.views.DashboardView;
import br.com.webbudget.vaadin.views.configuration.*;
import br.com.webbudget.vaadin.views.financial.*;
import br.com.webbudget.vaadin.views.journal.ListRefuelingsView;
import br.com.webbudget.vaadin.views.registration.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import jakarta.annotation.security.PermitAll;

@Layout
@PermitAll
public class MainLayout extends AppLayout {

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addToNavbar(createNavBar());
        addToDrawer(createNav());
    }

    private HorizontalLayout createNavBar() {
        var toggle = new DrawerToggle();
        var title = new H1("web::budget");
        title.getStyle()
            .set("font-size", "var(--lumo-font-size-l)")
            .set("margin", "0");
        var header = new HorizontalLayout(toggle, title);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        return header;
    }

    private SideNav createNav() {
        var nav = new SideNav();

        nav.addItem(new SideNavItem("Dashboard", DashboardView.class));

        var financial = new SideNavItem("Financial");
        financial.addItem(new SideNavItem("Period Movements", ListPeriodMovementsView.class));
        financial.addItem(new SideNavItem("Fixed Movements", ListFixedMovementsView.class));
        financial.addItem(new SideNavItem("Credit Card Invoices", ListCreditCardInvoicesView.class));
        financial.addItem(new SideNavItem("Transfers", FormTransferenceView.class));
        financial.addItem(new SideNavItem("Closing", FormClosingView.class));
        nav.addItem(financial);

        var registration = new SideNavItem("Registration");
        registration.addItem(new SideNavItem("Cards", ListCardsView.class));
        registration.addItem(new SideNavItem("Wallets", ListWalletsView.class));
        registration.addItem(new SideNavItem("Cost Centers", ListCostCentersView.class));
        registration.addItem(new SideNavItem("Movement Classes", ListMovementClassesView.class));
        registration.addItem(new SideNavItem("Contacts", ListContactsView.class));
        registration.addItem(new SideNavItem("Financial Periods", ListFinancialPeriodsView.class));
        registration.addItem(new SideNavItem("Vehicles", ListVehiclesView.class));
        nav.addItem(registration);

        var journal = new SideNavItem("Journal");
        journal.addItem(new SideNavItem("Refuelings", ListRefuelingsView.class));
        nav.addItem(journal);

        var configuration = new SideNavItem("Settings");
        configuration.addItem(new SideNavItem("Users", ListUsersView.class));
        configuration.addItem(new SideNavItem("Groups", ListGroupsView.class));
        configuration.addItem(new SideNavItem("Configuration", SettingsView.class));
        nav.addItem(configuration);

        return nav;
    }
}
