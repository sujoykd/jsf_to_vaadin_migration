package br.com.webbudget.vaadin.views.journal;

import br.com.webbudget.domain.entities.journal.Fuel;
import br.com.webbudget.domain.entities.journal.Refueling;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "journal/refuelings/detail/:id", layout = MainLayout.class)
@PageTitle("Refuelings")
public class DetailRefuelingView extends VerticalLayout implements BeforeEnterObserver {

    final Checkbox fullTankCheckbox = new Checkbox("Full Tank");
    final Checkbox enterFinancialCheckbox = new Checkbox("Enter Financial");
    final TextField refuelingDateField = new TextField("Refueling Date");
    final TextField odometerField = new TextField("Odometer");
    final TextField vehicleField = new TextField("Vehicle");
    final TextField locationField = new TextField("Location");
    final TextField movementClassField = new TextField("Class");
    final TextField financialPeriodField = new TextField("Entry Period");
    final Grid<Fuel> fuelsGrid = new Grid<>(Fuel.class, false);
    final Button deleteButton = new Button("Delete");
    final Button createMovementButton = new Button("Create Movement");
    final Button backButton = new Button("Back");

    private Refueling currentRefueling;
    private final DetailRefuelingPresenter presenter;

    public DetailRefuelingView(DetailRefuelingPresenter presenter) {
        this.presenter = presenter;

        setPadding(true);
        setSpacing(true);
        setMaxWidth("900px");

        fullTankCheckbox.setEnabled(false);
        enterFinancialCheckbox.setEnabled(false);

        refuelingDateField.setReadOnly(true);
        refuelingDateField.setWidthFull();

        odometerField.setReadOnly(true);
        odometerField.setWidthFull();

        vehicleField.setReadOnly(true);
        vehicleField.setWidthFull();

        locationField.setReadOnly(true);
        locationField.setWidthFull();

        movementClassField.setReadOnly(true);
        movementClassField.setWidthFull();

        financialPeriodField.setReadOnly(true);
        financialPeriodField.setWidthFull();

        var formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.add(fullTankCheckbox, enterFinancialCheckbox);
        formLayout.add(refuelingDateField, odometerField);
        formLayout.add(vehicleField, locationField);
        formLayout.add(movementClassField, financialPeriodField);

        fuelsGrid.addColumn(fuel -> fuel.getFuelType() != null ? fuel.getFuelType().name() : "")
                .setHeader("Type");
        fuelsGrid.addColumn(fuel -> fuel.getValuePerLiter() != null ? fuel.getValuePerLiter().toPlainString() : "")
                .setHeader("Value/L");
        fuelsGrid.addColumn(fuel -> fuel.getLiters() != null ? fuel.getLiters().toPlainString() : "")
                .setHeader("Liters");
        fuelsGrid.setHeight("200px");

        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> handleDelete());

        createMovementButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        createMovementButton.addClickListener(e -> handleCreateMovement());

        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("journal/refuelings"));

        var toolbar = new HorizontalLayout(deleteButton, createMovementButton, backButton);
        toolbar.setSpacing(true);

        add(formLayout, fuelsGrid, toolbar);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("id")
                .map(Long::parseLong)
                .flatMap(presenter::findById)
                .ifPresentOrElse(this::loadRefueling,
                        () -> event.forwardTo("journal/refuelings"));
    }

    private void loadRefueling(Refueling refueling) {
        currentRefueling = refueling;
        fullTankCheckbox.setValue(refueling.isFullTank());
        enterFinancialCheckbox.setValue(refueling.getPeriodMovement() != null);
        if (refueling.getEventDate() != null) refuelingDateField.setValue(refueling.getEventDate().toString());
        if (refueling.getOdometer() != null) odometerField.setValue(refueling.getOdometer().toString());
        if (refueling.getVehicle() != null) vehicleField.setValue(refueling.getVehicle().getIdentification() != null ? refueling.getVehicle().getIdentification() : "");
        if (refueling.getPlace() != null) locationField.setValue(refueling.getPlace());
        if (refueling.getMovementClass() != null) movementClassField.setValue(refueling.getMovementClass().getName() != null ? refueling.getMovementClass().getName() : "");
        if (refueling.getFinancialPeriod() != null) financialPeriodField.setValue(refueling.getFinancialPeriod().getIdentification() != null ? refueling.getFinancialPeriod().getIdentification() : "");
        fuelsGrid.setItems(refueling.getFuels());
        createMovementButton.setVisible(refueling.getPeriodMovement() == null);
    }

    private void handleDelete() {
        if (currentRefueling == null) return;
        var dialog = new ConfirmDialog();
        dialog.setHeader("Confirm Delete");
        dialog.setText("Are you sure you want to delete this refueling? This action cannot be undone.");
        dialog.setCancelable(true);
        dialog.setCancelText("No");
        dialog.setConfirmText("Yes");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(ev -> {
            presenter.delete(currentRefueling);
            UI.getCurrent().navigate("journal/refuelings");
        });
        dialog.open();
    }

    private void handleCreateMovement() {
        if (currentRefueling == null) return;
        presenter.createFinancialMovement(currentRefueling);
        var notification = Notification.show("Financial movement created successfully.");
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setDuration(3000);
        UI.getCurrent().navigate("journal/refuelings");
    }
}
