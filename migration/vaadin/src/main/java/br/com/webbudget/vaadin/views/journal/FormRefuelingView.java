package br.com.webbudget.vaadin.views.journal;

import br.com.webbudget.domain.entities.journal.Fuel;
import br.com.webbudget.domain.entities.journal.FuelType;
import br.com.webbudget.domain.entities.journal.Refueling;
import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.entities.registration.MovementClass;
import br.com.webbudget.domain.entities.registration.Vehicle;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.infrastructure.i18n.MessageSource;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.time.LocalDate;

@PermitAll
@Route(value = "journal/refuelings/form/:id?", layout = MainLayout.class)
@PageTitle("Refuelings")
public class FormRefuelingView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    final Checkbox fullTankCheckbox = new Checkbox("Full Tank");
    final Checkbox enterFinancialCheckbox = new Checkbox("Enter Financial");
    final DatePicker refuelingDatePicker = new DatePicker("Refueling Date");
    final IntegerField odometerField = new IntegerField("Odometer");
    final ComboBox<Vehicle> vehicleComboBox = new ComboBox<>("Vehicle");
    final TextField locationField = new TextField("Location");
    final ComboBox<MovementClass> movementClassComboBox = new ComboBox<>("Class");
    final ComboBox<FinancialPeriod> financialPeriodComboBox = new ComboBox<>("Entry Period");
    final Grid<Fuel> fuelsGrid = new Grid<>(Fuel.class, false);

    private Refueling currentRefueling;
    private Long editId;
    private final FormRefuelingPresenter presenter;

    public FormRefuelingView(FormRefuelingPresenter presenter) {
        this.presenter = presenter;

        setPadding(true);
        setSpacing(true);
        setMaxWidth("900px");

        vehicleComboBox.setItemLabelGenerator(v -> v.getIdentification() != null ? v.getIdentification() : "");
        vehicleComboBox.setItems(presenter.findAllVehicles());
        vehicleComboBox.setWidthFull();
        vehicleComboBox.addValueChangeListener(e -> onVehicleSelected(e.getValue()));

        movementClassComboBox.setItemLabelGenerator(mc -> mc.getName() != null ? mc.getName() : "");
        movementClassComboBox.setWidthFull();

        financialPeriodComboBox.setItemLabelGenerator(fp -> fp.getIdentification() != null ? fp.getIdentification() : "");
        financialPeriodComboBox.setItems(presenter.findOpenPeriods());
        financialPeriodComboBox.setWidthFull();

        refuelingDatePicker.setWidthFull();
        refuelingDatePicker.setValue(LocalDate.now());

        odometerField.setWidthFull();

        locationField.setWidthFull();

        // movementClass and financialPeriod are always required on the entity

        var formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.add(fullTankCheckbox, enterFinancialCheckbox);
        formLayout.add(refuelingDatePicker, odometerField);
        formLayout.add(vehicleComboBox, locationField);
        formLayout.add(movementClassComboBox, financialPeriodComboBox);

        fuelsGrid.addColumn(new ComponentRenderer<>(fuel -> {
            var litersField = new BigDecimalField();
            litersField.setValue(fuel.getLiters() != null ? fuel.getLiters() : BigDecimal.ZERO);
            litersField.addValueChangeListener(e -> {
                fuel.setLiters(e.getValue() != null ? e.getValue() : BigDecimal.ZERO);
                currentRefueling.totalsFuels();
            });
            return litersField;
        })).setHeader("Liters");

        fuelsGrid.addColumn(new ComponentRenderer<>(fuel -> {
            var priceField = new BigDecimalField();
            priceField.setValue(fuel.getValuePerLiter() != null ? fuel.getValuePerLiter() : BigDecimal.ZERO);
            priceField.addValueChangeListener(e -> {
                fuel.setValuePerLiter(e.getValue() != null ? e.getValue() : BigDecimal.ZERO);
                currentRefueling.totalsFuels();
            });
            return priceField;
        })).setHeader("Price/Liter");

        fuelsGrid.addColumn(new ComponentRenderer<>(fuel -> {
            var typeSelect = new ComboBox<FuelType>();
            typeSelect.setItems(FuelType.values());
            typeSelect.setItemLabelGenerator(FuelType::name);
            typeSelect.setValue(fuel.getFuelType());
            typeSelect.addValueChangeListener(e -> {
                if (e.getValue() != null) fuel.setFuelType(e.getValue());
            });
            return typeSelect;
        })).setHeader("Fuel Type");

        fuelsGrid.addColumn(new ComponentRenderer<>(fuel -> {
            var removeBtn = new Button("Remove");
            removeBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            removeBtn.addClickListener(e -> {
                currentRefueling.deleteFuel(fuel);
                refreshFuelsGrid();
            });
            return removeBtn;
        })).setHeader("");

        fuelsGrid.setHeight("200px");

        var addFuelBtn = new Button("Add Fuel", e -> {
            currentRefueling.addFuel();
            refreshFuelsGrid();
        });
        addFuelBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        var saveBtn = new Button("Save", e -> handleSave());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var backBtn = new Button("Back", e -> UI.getCurrent().navigate("journal/refuelings"));
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        var toolbar = new HorizontalLayout(saveBtn, backBtn);
        toolbar.setSpacing(true);

        add(formLayout, addFuelBtn, fuelsGrid, toolbar);

        initAddMode();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        editId = event.getRouteParameters().get("id")
                .map(Long::parseLong)
                .orElse(null);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        if (editId != null) {
            presenter.findById(editId).ifPresent(this::loadRefueling);
        }
    }

    private void initAddMode() {
        currentRefueling = new Refueling();
        fullTankCheckbox.setValue(true);
        enterFinancialCheckbox.setValue(false);
        refuelingDatePicker.setValue(LocalDate.now());
        odometerField.setValue(null);
        vehicleComboBox.clear();
        locationField.clear();
        movementClassComboBox.clear();
        financialPeriodComboBox.clear();
        refreshFuelsGrid();
    }

    private void loadRefueling(Refueling refueling) {
        currentRefueling = refueling;
        fullTankCheckbox.setValue(refueling.isFullTank());
        if (refueling.getEventDate() != null) refuelingDatePicker.setValue(refueling.getEventDate());
        if (refueling.getOdometer() != null) odometerField.setValue(refueling.getOdometer().intValue());
        vehicleComboBox.setValue(refueling.getVehicle());
        if (refueling.getPlace() != null) locationField.setValue(refueling.getPlace());
        if (refueling.getMovementClass() != null) {
            onVehicleSelected(refueling.getVehicle());
            movementClassComboBox.setValue(refueling.getMovementClass());
        }
        financialPeriodComboBox.setValue(refueling.getFinancialPeriod());
        enterFinancialCheckbox.setValue(refueling.getPeriodMovement() != null);
        refreshFuelsGrid();
    }

    private void onVehicleSelected(Vehicle vehicle) {
        movementClassComboBox.clear();
        if (vehicle != null) {
            movementClassComboBox.setItems(presenter.findMovementClassesByVehicle(vehicle));
        }
    }

    private void refreshFuelsGrid() {
        if (currentRefueling != null) {
            fuelsGrid.setItems(currentRefueling.getFuels());
        }
    }

    private void handleSave() {
        if (refuelingDatePicker.getValue() == null) {
            refuelingDatePicker.setInvalid(true);
            return;
        }
        if (odometerField.getValue() == null) {
            odometerField.setInvalid(true);
            return;
        }
        if (vehicleComboBox.getValue() == null) {
            vehicleComboBox.setInvalid(true);
            return;
        }
        if (currentRefueling.getFuels().isEmpty()) {
            Notification.show("At least one fuel entry is required.")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (movementClassComboBox.getValue() == null) {
            movementClassComboBox.setInvalid(true);
            return;
        }
        if (financialPeriodComboBox.getValue() == null) {
            financialPeriodComboBox.setInvalid(true);
            return;
        }

        boolean shouldCreateMovement = enterFinancialCheckbox.getValue();

        currentRefueling.setFullTank(fullTankCheckbox.getValue());
        currentRefueling.setEventDate(refuelingDatePicker.getValue());
        currentRefueling.setOdometer(odometerField.getValue().longValue());
        currentRefueling.setVehicle(vehicleComboBox.getValue());
        currentRefueling.setPlace(locationField.getValue());
        currentRefueling.setMovementClass(movementClassComboBox.getValue());
        currentRefueling.setFinancialPeriod(financialPeriodComboBox.getValue());

        try {
            presenter.save(currentRefueling, shouldCreateMovement);
            var notification = Notification.show("Refueling saved successfully.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(3000);
            initAddMode();
        } catch (BusinessLogicException ex) {
            var notification = Notification.show(MessageSource.get(ex.getMessage()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        }
    }
}
