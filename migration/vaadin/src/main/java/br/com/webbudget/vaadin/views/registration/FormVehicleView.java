package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.domain.entities.registration.Vehicle;
import br.com.webbudget.domain.entities.registration.VehicleType;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.infrastructure.i18n.MessageSource;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "registration/vehicles/form/:id?", layout = MainLayout.class)
@PageTitle("Vehicles")
public class FormVehicleView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    final Checkbox activeCheckbox = new Checkbox("Active");
    final Select<VehicleType> vehicleTypeSelect = new Select<>();
    final TextField identificationField = new TextField("Identification");
    final TextField brandField = new TextField("Brand");
    final TextField modelField = new TextField("Model");
    final ComboBox<CostCenter> costCenterComboBox = new ComboBox<>("Cost Center");
    final TextField licensePlateField = new TextField("License Plate");
    final IntegerField modelYearField = new IntegerField("Model Year");
    final IntegerField manufacturingYearField = new IntegerField("Manufacturing Year");
    final IntegerField odometerField = new IntegerField("Odometer");
    final IntegerField fuelCapacityField = new IntegerField("Tank Cap. (liters)");

    final Button saveButton = new Button("Save");
    final Button updateButton = new Button("Update");
    final Button backButton = new Button("Back");

    private boolean editMode = false;
    private Vehicle currentVehicle;

    private final FormVehiclePresenter presenter;

    public FormVehicleView(FormVehiclePresenter presenter) {
        this.presenter = presenter;

        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");

        vehicleTypeSelect.setLabel("Type");
        vehicleTypeSelect.setItems(VehicleType.values());
        vehicleTypeSelect.setItemLabelGenerator(vt -> vt != null ? MessageSource.get(vt.toString()) : "");
        vehicleTypeSelect.setWidthFull();
        vehicleTypeSelect.setRequiredIndicatorVisible(true);

        costCenterComboBox.setItemLabelGenerator(c -> c != null ? c.getName() : "");
        costCenterComboBox.setItems(presenter.findActiveCostCenters());
        costCenterComboBox.setWidthFull();
        costCenterComboBox.setRequiredIndicatorVisible(true);

        identificationField.setWidthFull();
        identificationField.setRequired(true);

        brandField.setWidthFull();
        brandField.setRequired(true);

        modelField.setWidthFull();
        modelField.setRequired(true);

        licensePlateField.setWidthFull();
        licensePlateField.setRequired(true);

        modelYearField.setWidthFull();
        manufacturingYearField.setWidthFull();

        odometerField.setWidthFull();
        odometerField.setRequiredIndicatorVisible(true);
        odometerField.setMin(0);

        fuelCapacityField.setWidthFull();
        fuelCapacityField.setRequiredIndicatorVisible(true);
        fuelCapacityField.setMin(0);

        var formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        formLayout.add(identificationField, vehicleTypeSelect);
        formLayout.add(brandField, modelField);
        formLayout.add(licensePlateField, costCenterComboBox);
        formLayout.add(modelYearField, manufacturingYearField);
        formLayout.add(odometerField, fuelCapacityField);
        formLayout.add(activeCheckbox, 2);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> onSave());

        updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateButton.addClickListener(e -> onUpdate());
        updateButton.setVisible(false);

        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("registration/vehicles"));

        var toolbar = new HorizontalLayout(saveButton, updateButton, backButton);
        toolbar.setSpacing(true);

        add(formLayout, toolbar);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var idParam = event.getRouteParameters().get("id");
        if (idParam.isPresent()) {
            long id = Long.parseLong(idParam.get());
            presenter.findById(id).ifPresentOrElse(vehicle -> {
                currentVehicle = vehicle;
                editMode = true;
            }, () -> event.forwardTo("registration/vehicles"));
        } else {
            currentVehicle = new Vehicle();
            editMode = false;
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        populateForm(currentVehicle);
        saveButton.setVisible(!editMode);
        updateButton.setVisible(editMode);
    }

    private void populateForm(Vehicle vehicle) {
        activeCheckbox.setValue(vehicle.isActive());
        vehicleTypeSelect.setValue(vehicle.getVehicleType());
        identificationField.setValue(vehicle.getIdentification() != null ? vehicle.getIdentification() : "");
        brandField.setValue(vehicle.getBrand() != null ? vehicle.getBrand() : "");
        modelField.setValue(vehicle.getModel() != null ? vehicle.getModel() : "");
        costCenterComboBox.setValue(vehicle.getCostCenter());
        licensePlateField.setValue(vehicle.getLicensePlate() != null ? vehicle.getLicensePlate() : "");
        modelYearField.setValue(vehicle.getModelYear() != null ? vehicle.getModelYear() : 0);
        manufacturingYearField.setValue(vehicle.getManufacturingYear() != null ? vehicle.getManufacturingYear() : 0);
        odometerField.setValue(vehicle.getOdometer() != null ? vehicle.getOdometer().intValue() : 0);
        fuelCapacityField.setValue(vehicle.getFuelCapacity() != null ? vehicle.getFuelCapacity() : 0);
    }

    private boolean validateFields() {
        boolean valid = true;

        if (identificationField.getValue() == null || identificationField.getValue().isBlank()) {
            identificationField.setInvalid(true);
            identificationField.setErrorMessage("Identification is required");
            valid = false;
        } else {
            identificationField.setInvalid(false);
        }

        if (brandField.getValue() == null || brandField.getValue().isBlank()) {
            brandField.setInvalid(true);
            brandField.setErrorMessage("Brand is required");
            valid = false;
        } else {
            brandField.setInvalid(false);
        }

        if (modelField.getValue() == null || modelField.getValue().isBlank()) {
            modelField.setInvalid(true);
            modelField.setErrorMessage("Model is required");
            valid = false;
        } else {
            modelField.setInvalid(false);
        }

        if (vehicleTypeSelect.getValue() == null) {
            vehicleTypeSelect.setInvalid(true);
            vehicleTypeSelect.setErrorMessage("Type is required");
            valid = false;
        } else {
            vehicleTypeSelect.setInvalid(false);
        }

        if (costCenterComboBox.getValue() == null) {
            costCenterComboBox.setInvalid(true);
            costCenterComboBox.setErrorMessage("Cost Center is required");
            valid = false;
        } else {
            costCenterComboBox.setInvalid(false);
        }

        if (licensePlateField.getValue() == null || licensePlateField.getValue().isBlank()) {
            licensePlateField.setInvalid(true);
            licensePlateField.setErrorMessage("License Plate is required");
            valid = false;
        } else {
            licensePlateField.setInvalid(false);
        }

        if (odometerField.getValue() == null) {
            odometerField.setInvalid(true);
            odometerField.setErrorMessage("Odometer is required");
            valid = false;
        } else {
            odometerField.setInvalid(false);
        }

        if (fuelCapacityField.getValue() == null) {
            fuelCapacityField.setInvalid(true);
            fuelCapacityField.setErrorMessage("Tank capacity is required");
            valid = false;
        } else {
            fuelCapacityField.setInvalid(false);
        }

        return valid;
    }

    private Vehicle buildEntity() {
        if (currentVehicle == null) {
            currentVehicle = new Vehicle();
        }
        currentVehicle.setActive(activeCheckbox.getValue());
        currentVehicle.setVehicleType(vehicleTypeSelect.getValue());
        currentVehicle.setIdentification(identificationField.getValue());
        currentVehicle.setBrand(brandField.getValue());
        currentVehicle.setModel(modelField.getValue());
        currentVehicle.setCostCenter(costCenterComboBox.getValue());
        currentVehicle.setLicensePlate(licensePlateField.getValue());
        currentVehicle.setModelYear(modelYearField.getValue());
        currentVehicle.setManufacturingYear(manufacturingYearField.getValue());
        Integer odometerVal = odometerField.getValue();
        currentVehicle.setOdometer(odometerVal != null ? odometerVal.longValue() : 0L);
        currentVehicle.setFuelCapacity(fuelCapacityField.getValue());
        return currentVehicle;
    }

    private void onSave() {
        if (!validateFields()) {
            return;
        }
        try {
            presenter.save(buildEntity());
            currentVehicle = new Vehicle();
            populateForm(currentVehicle);
            var notification = Notification.show("Vehicle saved successfully.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(3000);
        } catch (BusinessLogicException ex) {
            var notification = Notification.show(MessageSource.get(ex.getMessage()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        }
    }

    private void onUpdate() {
        if (!validateFields()) {
            return;
        }
        try {
            currentVehicle = presenter.update(buildEntity());
            var notification = Notification.show("Vehicle updated successfully.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(3000);
        } catch (BusinessLogicException ex) {
            var notification = Notification.show(MessageSource.get(ex.getMessage()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        }
    }
}
