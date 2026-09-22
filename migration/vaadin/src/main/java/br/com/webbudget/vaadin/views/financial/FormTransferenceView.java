package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.Transference;
import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@PermitAll
@Route(value = "financial/transferences", layout = MainLayout.class)
@PageTitle("Transfers")
public class FormTransferenceView extends VerticalLayout {

    final ComboBox<Wallet> originComboBox = new ComboBox<>("Origin Wallet");
    final ComboBox<Wallet> destinationComboBox = new ComboBox<>("Destination Wallet");
    final DatePicker transferDatePicker = new DatePicker("Date");
    final BigDecimalField valueField = new BigDecimalField("Amount");
    final TextArea descriptionArea = new TextArea("Description");

    private final BeanValidationBinder<Transference> binder = new BeanValidationBinder<>(Transference.class);

    public FormTransferenceView(FormTransferencePresenter presenter) {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("800px");

        List<Wallet> wallets = presenter.loadWallets();

        originComboBox.setItemLabelGenerator(Wallet::getName);
        originComboBox.setItems(wallets);
        originComboBox.setRequired(true);
        originComboBox.setWidthFull();

        destinationComboBox.setItemLabelGenerator(Wallet::getName);
        destinationComboBox.setItems(wallets);
        destinationComboBox.setRequired(true);
        destinationComboBox.setWidthFull();

        transferDatePicker.setRequired(true);
        transferDatePicker.setWidthFull();

        valueField.setRequired(true);
        valueField.setWidthFull();

        descriptionArea.setWidthFull();
        descriptionArea.setMinHeight("80px");

        binder.forField(originComboBox)
                .asRequired("Origin wallet is required")
                .bind(Transference::getOrigin, Transference::setOrigin);

        binder.forField(destinationComboBox)
                .asRequired("Destination wallet is required")
                .bind(Transference::getDestination, Transference::setDestination);

        binder.forField(transferDatePicker)
                .asRequired("Date is required")
                .bind(Transference::getTransferDate, Transference::setTransferDate);

        binder.forField(valueField)
                .asRequired("Amount is required")
                .bind(Transference::getValue, Transference::setValue);

        binder.forField(descriptionArea)
                .bind(Transference::getDescription, Transference::setDescription);

        binder.setBean(new Transference());

        var transferBtn = new Button("Transfer", e -> {
            if (binder.validate().isOk()) {
                Transference transference = binder.getBean();
                presenter.transfer(transference);
                binder.setBean(new Transference());
                var notification = Notification.show("Transfer completed successfully.");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setDuration(3000);
            }
        });
        transferBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var historyBtn = new Button("History", e -> UI.getCurrent().navigate(TransferenceHistoricView.class));
        historyBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        var formRow1 = new HorizontalLayout(originComboBox, destinationComboBox);
        formRow1.setWidthFull();
        formRow1.expand(originComboBox, destinationComboBox);

        var formRow2 = new HorizontalLayout(transferDatePicker, valueField);
        formRow2.setWidthFull();
        formRow2.expand(transferDatePicker, valueField);

        var toolbar = new HorizontalLayout(transferBtn, historyBtn);
        toolbar.setSpacing(true);

        add(formRow1, formRow2, descriptionArea, toolbar);
    }
}
