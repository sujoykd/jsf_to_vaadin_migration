package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.Vehicle;
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
@Route(value = "registration/vehicles/detail/:id", layout = MainLayout.class)
@PageTitle("Vehicles")
public class DetailVehicleView extends VerticalLayout implements BeforeEnterObserver {

    final Checkbox activeCheckbox = new Checkbox("Active");
    final TextField typeField = new TextField("Type");
    final TextField identificationField = new TextField("Identification");
    final TextField brandField = new TextField("Brand");
    final TextField modelField = new TextField("Model");
    final TextField costCenterField = new TextField("Cost Center");
    final TextField licensePlateField = new TextField("License Plate");
    final TextField modelYearField = new TextField("Model Year");
    final TextField manufacturingYearField = new TextField("Manufacturing Year");
    final TextField odometerField = new TextField("Odometer");
    final TextField fuelCapacityField = new TextField("Tank Cap. (liters)");
    final Button editButton = new Button("Edit");
    final Button deleteButton = new Button("Delete");
    final Button backButton = new Button("Back");

    private Vehicle currentVehicle;
    private final DetailVehiclePresenter presenter;

    public DetailVehicleView(DetailVehiclePresenter presenter) {
        this.presenter = presenter;
        setPadding(true); setSpacing(true); setMaxWidth("900px");
        activeCheckbox.setEnabled(false);
        for (var f : new TextField[]{typeField, identificationField, brandField, modelField, costCenterField,
                licensePlateField, modelYearField, manufacturingYearField, odometerField, fuelCapacityField}) {
            f.setReadOnly(true); f.setWidthFull();
        }

        var formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));
        formLayout.add(activeCheckbox);
        formLayout.add(typeField, identificationField);
        formLayout.add(brandField, modelField);
        formLayout.add(costCenterField, licensePlateField);
        formLayout.add(modelYearField, manufacturingYearField);
        formLayout.add(odometerField, fuelCapacityField);

        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e -> { if (currentVehicle != null) UI.getCurrent().navigate(FormVehicleView.class, new RouteParameters("id", String.valueOf(currentVehicle.getId()))); });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> handleDelete());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("registration/vehicles"));

        add(formLayout, new HorizontalLayout(editButton, deleteButton, backButton));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("id").map(Long::parseLong).flatMap(presenter::findById)
                .ifPresentOrElse(this::loadVehicle, () -> event.forwardTo("registration/vehicles"));
    }

    private void loadVehicle(Vehicle v) {
        currentVehicle = v;
        activeCheckbox.setValue(v.isActive());
        typeField.setValue(v.getVehicleType() != null ? v.getVehicleType().name() : "");
        identificationField.setValue(v.getIdentification() != null ? v.getIdentification() : "");
        brandField.setValue(v.getBrand() != null ? v.getBrand() : "");
        modelField.setValue(v.getModel() != null ? v.getModel() : "");
        costCenterField.setValue(v.getCostCenter() != null && v.getCostCenter().getName() != null ? v.getCostCenter().getName() : "");
        licensePlateField.setValue(v.getLicensePlate() != null ? v.getLicensePlate() : "");
        modelYearField.setValue(v.getModelYear() != null ? v.getModelYear().toString() : "");
        manufacturingYearField.setValue(v.getManufacturingYear() != null ? v.getManufacturingYear().toString() : "");
        odometerField.setValue(v.getOdometer() != null ? v.getOdometer().toString() : "");
        fuelCapacityField.setValue(v.getFuelCapacity() != null ? v.getFuelCapacity().toString() : "");
    }

    private void handleDelete() {
        if (currentVehicle == null) return;
        var dialog = new ConfirmDialog();
        dialog.setHeader("Confirm Delete");
        dialog.setText("Delete vehicle '" + currentVehicle.getIdentification() + "'?");
        dialog.setCancelable(true); dialog.setCancelText("No"); dialog.setConfirmText("Yes");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(ev -> { presenter.delete(currentVehicle); UI.getCurrent().navigate("registration/vehicles"); });
        dialog.open();
    }
}
