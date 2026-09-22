package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
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
@Route(value = "registration/wallets/detail/:id", layout = MainLayout.class)
@PageTitle("Wallets")
public class DetailWalletView extends VerticalLayout implements BeforeEnterObserver {

    final Checkbox activeCheckbox = new Checkbox("Active");
    final TextField nameField = new TextField("Name");
    final TextField walletTypeField = new TextField("Wallet Type");
    final TextField bankField = new TextField("Bank");
    final TextField branchField = new TextField("Branch");
    final TextField accountField = new TextField("Account");
    final TextField checkDigitField = new TextField("Check Digit");
    final TextField balanceField = new TextField("Balance");
    final TextArea descriptionArea = new TextArea("Description");
    final Button editButton = new Button("Edit");
    final Button deleteButton = new Button("Delete");
    final Button backButton = new Button("Back");

    private Wallet currentWallet;
    private final DetailWalletPresenter presenter;

    public DetailWalletView(DetailWalletPresenter presenter) {
        this.presenter = presenter;
        setPadding(true); setSpacing(true); setMaxWidth("900px");
        activeCheckbox.setEnabled(false);
        for (var f : new TextField[]{nameField, walletTypeField, bankField, branchField, accountField, checkDigitField, balanceField}) {
            f.setReadOnly(true); f.setWidthFull();
        }
        descriptionArea.setReadOnly(true); descriptionArea.setWidthFull();

        var formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));
        formLayout.add(activeCheckbox);
        formLayout.add(nameField, walletTypeField);
        formLayout.add(bankField, branchField);
        formLayout.add(accountField, checkDigitField);
        formLayout.add(balanceField);
        formLayout.add(descriptionArea, 2);

        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e -> { if (currentWallet != null) UI.getCurrent().navigate(FormWalletView.class, new RouteParameters("id", String.valueOf(currentWallet.getId()))); });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> handleDelete());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("registration/wallets"));

        add(formLayout, new HorizontalLayout(editButton, deleteButton, backButton));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("id").map(Long::parseLong).flatMap(presenter::findById)
                .ifPresentOrElse(this::loadWallet, () -> event.forwardTo("registration/wallets"));
    }

    private void loadWallet(Wallet wallet) {
        currentWallet = wallet;
        activeCheckbox.setValue(wallet.isActive());
        nameField.setValue(wallet.getName() != null ? wallet.getName() : "");
        walletTypeField.setValue(wallet.getWalletType() != null ? wallet.getWalletType().name() : "");
        bankField.setValue(wallet.getBank() != null ? wallet.getBank() : "");
        branchField.setValue(wallet.getAgency() != null ? wallet.getAgency() : "");
        accountField.setValue(wallet.getAccount() != null ? wallet.getAccount() : "");
        checkDigitField.setValue(wallet.getDigit() != null ? wallet.getDigit() : "");
        balanceField.setValue(wallet.getActualBalance() != null ? wallet.getActualBalance().toPlainString() : "");
        descriptionArea.setValue(wallet.getDescription() != null ? wallet.getDescription() : "");
    }

    private void handleDelete() {
        if (currentWallet == null) return;
        var dialog = new ConfirmDialog();
        dialog.setHeader("Confirm Delete");
        dialog.setText("Delete wallet '" + currentWallet.getName() + "'?");
        dialog.setCancelable(true); dialog.setCancelText("No"); dialog.setConfirmText("Yes");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(ev -> { presenter.delete(currentWallet); UI.getCurrent().navigate("registration/wallets"); });
        dialog.open();
    }
}
