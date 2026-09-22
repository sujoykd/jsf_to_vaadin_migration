package br.com.webbudget.vaadin.views.configuration;

import br.com.webbudget.domain.entities.configuration.User;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
@Route(value = "configuration/users/:id", layout = MainLayout.class)
@PageTitle("Users")
public class DetailUserView extends VerticalLayout implements BeforeEnterObserver {

    final TextField nameField = new TextField("Name");
    final TextField usernameField = new TextField("Username");
    final TextField emailField = new TextField("E-mail");
    final Checkbox activeCheckbox = new Checkbox("Active");
    final TextField groupField = new TextField("Group");
    final TextField storeTypeField = new TextField("Authentication");

    private User currentUser;

    private final DetailUserPresenter presenter;

    public DetailUserView(DetailUserPresenter presenter) {
        this.presenter = presenter;

        setPadding(true);
        setSpacing(true);
        setMaxWidth("800px");

        nameField.setReadOnly(true);
        nameField.setWidthFull();

        usernameField.setReadOnly(true);
        usernameField.setWidthFull();

        emailField.setReadOnly(true);
        emailField.setWidthFull();

        activeCheckbox.setReadOnly(true);

        groupField.setReadOnly(true);
        groupField.setWidthFull();

        storeTypeField.setReadOnly(true);
        storeTypeField.setWidthFull();

        var form = new FormLayout(nameField, usernameField, emailField, groupField, storeTypeField, activeCheckbox);
        form.setWidthFull();

        var editBtn = new Button("Edit", e -> {
            if (currentUser != null) {
                UI.getCurrent().navigate(FormUserView.class,
                        new RouteParameters("id", String.valueOf(currentUser.getId())));
            }
        });
        editBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var deleteBtn = new Button("Delete", e -> confirmDelete());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        var backBtn = new Button("Back", e -> UI.getCurrent().navigate("configuration/users"));
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        var toolbar = new HorizontalLayout(editBtn, deleteBtn, backBtn);
        toolbar.setSpacing(true);

        add(form, toolbar);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("id")
                .map(Long::parseLong)
                .flatMap(presenter::findById)
                .ifPresentOrElse(this::loadUser, () -> event.forwardTo("configuration/users"));
    }

    private void loadUser(User user) {
        this.currentUser = user;
        nameField.setValue(user.getName() != null ? user.getName() : "");
        usernameField.setValue(user.getUsername() != null ? user.getUsername() : "");
        emailField.setValue(user.getEmail() != null ? user.getEmail() : "");
        activeCheckbox.setValue(user.isActive());
        groupField.setValue(user.getGroup() != null ? user.getGroup().getName() : "");
        storeTypeField.setValue(user.getStoreType() != null ? user.getStoreType().name() : "");
    }

    private void confirmDelete() {
        var dialog = new ConfirmDialog();
        dialog.setHeader("Delete User");
        dialog.setText("Are you sure you want to delete this user? This action cannot be undone.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            presenter.delete(currentUser);
            var notification = Notification.show("User deleted successfully.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(3000);
            UI.getCurrent().navigate("configuration/users");
        });
        dialog.open();
    }
}
