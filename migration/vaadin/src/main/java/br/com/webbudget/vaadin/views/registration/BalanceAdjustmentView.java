package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.math.BigDecimal;

@PermitAll
@Route(value = "registration/wallets/balance-adjustment/:id", layout = MainLayout.class)
@PageTitle("Balance Adjustment")
public class BalanceAdjustmentView extends VerticalLayout implements BeforeEnterObserver {

    final TextField currentBalanceField = new TextField("Current Balance");
    final BigDecimalField adjustmentField = new BigDecimalField("Adjustment Amount");
    final TextArea reasonArea = new TextArea("Reason");
    final Button saveButton = new Button("Save");
    final Button backButton = new Button("Back");

    private Wallet currentWallet;
    private final BalanceAdjustmentPresenter presenter;

    public BalanceAdjustmentView(BalanceAdjustmentPresenter presenter) {
        this.presenter = presenter;
        setPadding(true); setSpacing(true); setMaxWidth("600px");
        currentBalanceField.setReadOnly(true); currentBalanceField.setWidthFull();
        adjustmentField.setWidthFull();
        reasonArea.setMaxLength(150); reasonArea.setWidthFull();

        var formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        formLayout.add(currentBalanceField, adjustmentField, reasonArea);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> handleSave());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("registration/wallets"));

        add(formLayout, new HorizontalLayout(saveButton, backButton));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("id").map(Long::parseLong).flatMap(presenter::findById)
                .ifPresentOrElse(this::loadWallet, () -> event.forwardTo("registration/wallets"));
    }

    private void loadWallet(Wallet wallet) {
        currentWallet = wallet;
        currentBalanceField.setValue(wallet.getActualBalance() != null ? wallet.getActualBalance().toPlainString() : "0");
    }

    private void handleSave() {
        if (currentWallet == null) return;
        BigDecimal amount = adjustmentField.getValue();
        if (amount == null) {
            adjustmentField.setInvalid(true);
            return;
        }
        String reason = reasonArea.getValue();
        presenter.adjustBalance(currentWallet, amount, reason);
        var notification = Notification.show("Balance adjusted successfully.");
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setDuration(3000);
        UI.getCurrent().navigate("registration/wallets");
    }
}
