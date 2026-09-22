package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.PeriodMovement;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
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
@Route(value = "financial/period-movements/:id", layout = MainLayout.class)
@PageTitle("Period Movements")
public class DetailPeriodMovementView extends VerticalLayout implements BeforeEnterObserver {

    final TextField identificationField = new TextField("Identification");
    final TextField financialPeriodField = new TextField("Financial Period");
    final TextField dueDateField = new TextField("Due Date");
    final TextField amountField = new TextField("Amount");
    final TextField contactField = new TextField("Contact");
    final TextArea descriptionArea = new TextArea("Description");
    final TextField stateField = new TextField("State");

    private PeriodMovement currentMovement;

    private final DetailPeriodMovementPresenter presenter;

    public DetailPeriodMovementView(DetailPeriodMovementPresenter presenter) {
        this.presenter = presenter;

        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");

        identificationField.setReadOnly(true);
        identificationField.setWidthFull();

        financialPeriodField.setReadOnly(true);
        financialPeriodField.setWidthFull();

        dueDateField.setReadOnly(true);
        dueDateField.setWidthFull();

        amountField.setReadOnly(true);
        amountField.setWidthFull();

        contactField.setReadOnly(true);
        contactField.setWidthFull();

        descriptionArea.setReadOnly(true);
        descriptionArea.setWidthFull();

        stateField.setReadOnly(true);
        stateField.setWidthFull();

        var formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );
        formLayout.add(identificationField, financialPeriodField);
        formLayout.add(dueDateField, amountField);
        formLayout.add(contactField, stateField);
        formLayout.add(descriptionArea, 2);

        var editButton = new Button("Edit", e -> {
            if (currentMovement != null) {
                UI.getCurrent().navigate(FormPeriodMovementView.class,
                        new RouteParameters("id", String.valueOf(currentMovement.getId())));
            }
        });
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var payButton = new Button("Pay", e -> {
            if (currentMovement != null) {
                UI.getCurrent().navigate(FormPaymentView.class,
                        new RouteParameters("id", String.valueOf(currentMovement.getId())));
            }
        });
        payButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        var deleteButton = new Button("Delete", e -> {
            if (currentMovement != null) {
                var dialog = new ConfirmDialog();
                dialog.setHeader("Confirm Delete");
                dialog.setText("Are you sure you want to delete the movement '"
                        + currentMovement.getIdentification() + "'? This action cannot be undone.");
                dialog.setCancelable(true);
                dialog.setCancelText("Cancel");
                dialog.setConfirmText("Delete");
                dialog.setConfirmButtonTheme("error primary");
                dialog.addConfirmListener(event -> {
                    presenter.delete(currentMovement);
                    UI.getCurrent().navigate("financial/period-movements");
                });
                dialog.open();
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        var backButton = new Button("Back", e -> UI.getCurrent().navigate("financial/period-movements"));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        var toolbar = new HorizontalLayout(editButton, payButton, deleteButton, backButton);
        toolbar.setSpacing(true);

        add(formLayout, toolbar);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var idParam = event.getRouteParameters().get("id");
        if (idParam.isPresent()) {
            long id = Long.parseLong(idParam.get());
            presenter.findById(id).ifPresentOrElse(pm -> {
                currentMovement = pm;
                loadPeriodMovement(pm);
            }, () -> event.forwardTo("financial/period-movements"));
        } else {
            event.forwardTo("financial/period-movements");
        }
    }

    private void loadPeriodMovement(PeriodMovement pm) {
        identificationField.setValue(pm.getIdentification() != null ? pm.getIdentification() : "");
        financialPeriodField.setValue(pm.getFinancialPeriod() != null
                ? pm.getFinancialPeriod().getIdentification() : "");
        dueDateField.setValue(pm.getDueDate() != null ? pm.getDueDate().toString() : "");
        amountField.setValue(pm.getValue() != null ? pm.getValue().toPlainString() : "");
        contactField.setValue(pm.getContact() != null ? pm.getContact().getName() : "");
        descriptionArea.setValue(pm.getDescription() != null ? pm.getDescription() : "");
        stateField.setValue(pm.getPeriodMovementState() != null ? pm.getPeriodMovementState().toString() : "");
    }
}
