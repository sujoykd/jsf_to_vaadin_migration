package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.FixedMovement;
import br.com.webbudget.domain.entities.financial.Launch;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "financial/fixed-movements/:id", layout = MainLayout.class)
@PageTitle("Fixed Movements")
public class DetailFixedMovementView extends VerticalLayout implements BeforeEnterObserver {

    final TextField identificationField = new TextField("Identification");
    final TextField valueField = new TextField("Value");
    final TextField startDateField = new TextField("Start");
    final TextField contactField = new TextField("Contact");
    final TextField statusField = new TextField("Status");
    final TextField totalQuotesField = new TextField("Total Installments");
    final Checkbox autoLaunchCheckbox = new Checkbox("Automatic Entry");
    final Checkbox undeterminedCheckbox = new Checkbox("Undetermined");
    final TextArea descriptionArea = new TextArea("Description");
    final Grid<Launch> launchesGrid = new Grid<>(Launch.class, false);

    private FixedMovement currentFixedMovement;
    private final DetailFixedMovementPresenter presenter;

    public DetailFixedMovementView(DetailFixedMovementPresenter presenter) {
        this.presenter = presenter;

        setPadding(true);
        setSpacing(true);
        setMaxWidth("900px");

        identificationField.setReadOnly(true);
        identificationField.setWidthFull();

        valueField.setReadOnly(true);
        valueField.setWidthFull();

        startDateField.setReadOnly(true);
        startDateField.setWidthFull();

        contactField.setReadOnly(true);
        contactField.setWidthFull();

        statusField.setReadOnly(true);
        statusField.setWidthFull();

        totalQuotesField.setReadOnly(true);
        totalQuotesField.setWidthFull();

        autoLaunchCheckbox.setReadOnly(true);
        undeterminedCheckbox.setReadOnly(true);

        descriptionArea.setReadOnly(true);
        descriptionArea.setWidthFull();

        launchesGrid.addColumn(l -> l.getFinancialPeriod() != null ? l.getFinancialPeriod().getIdentification() : "")
                .setHeader("Financial Period");
        launchesGrid.addColumn(l -> l.getPeriodMovement() != null ? l.getPeriodMovement().getIdentification() : "")
                .setHeader("Movement");
        launchesGrid.addColumn(l -> l.getPeriodMovement() != null ? l.getPeriodMovement().getValue() : "")
                .setHeader("Amount");
        launchesGrid.setHeight("200px");

        var form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        form.add(identificationField, valueField, startDateField, contactField, statusField, totalQuotesField,
                autoLaunchCheckbox, undeterminedCheckbox);
        form.setColspan(descriptionArea, 2);
        form.add(descriptionArea);

        var editButton = new Button("Edit", e -> {
            if (currentFixedMovement != null) {
                UI.getCurrent().navigate(FormFixedMovementView.class,
                        new RouteParameters("id", String.valueOf(currentFixedMovement.getId())));
            }
        });
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var deleteButton = new Button("Delete", e -> {
            var confirm = new ConfirmDialog();
            confirm.setHeader("Confirm Delete");
            confirm.setText("Are you sure you want to delete this fixed movement?");
            confirm.setConfirmText("Delete");
            confirm.setConfirmButtonTheme("error primary");
            confirm.setCancelText("Cancel");
            confirm.setCancelable(true);
            confirm.addConfirmListener(ce -> {
                presenter.delete(currentFixedMovement);
                UI.getCurrent().navigate("financial/fixed-movements");
            });
            confirm.open();
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        var backButton = new Button("Back", e -> UI.getCurrent().navigate("financial/fixed-movements"));

        var toolbar = new HorizontalLayout(editButton, deleteButton, backButton);
        toolbar.setSpacing(true);

        add(toolbar, form, launchesGrid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("id")
                .map(Long::parseLong)
                .flatMap(presenter::findById)
                .ifPresentOrElse(this::loadFixedMovement, () -> event.forwardTo("financial/fixed-movements"));
    }

    void loadFixedMovement(FixedMovement fm) {
        this.currentFixedMovement = fm;
        identificationField.setValue(fm.getIdentification() != null ? fm.getIdentification() : "");
        valueField.setValue(fm.getValue() != null ? fm.getValue().toString() : "");
        startDateField.setValue(fm.getStartDate() != null ? fm.getStartDate().toString() : "");
        contactField.setValue(fm.getContact() != null ? fm.getContact().getName() : "");
        statusField.setValue(fm.getFixedMovementState() != null ? fm.getFixedMovementState().toString() : "");
        totalQuotesField.setValue(fm.getTotalQuotes() != null ? fm.getTotalQuotes().toString() : "");
        autoLaunchCheckbox.setValue(fm.isAutoLaunch());
        undeterminedCheckbox.setValue(fm.isUndetermined());
        descriptionArea.setValue(fm.getDescription() != null ? fm.getDescription() : "");
        launchesGrid.setItems(presenter.findLaunches(fm));
    }
}
