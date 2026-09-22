package br.com.webbudget.vaadin.views.configuration;

import br.com.webbudget.domain.entities.configuration.Group;
import br.com.webbudget.domain.entities.configuration.StoreType;
import br.com.webbudget.domain.entities.configuration.User;
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
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "configuration/users/form/:id?", layout = MainLayout.class)
@PageTitle("Users")
public class FormUserView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    final TextField nameField = new TextField("Name");
    final TextField usernameField = new TextField("Username");
    final TextField emailField = new TextField("E-mail");
    final PasswordField passwordField = new PasswordField("Password");
    final PasswordField passwordConfirmField = new PasswordField("Password Confirmation");
    final Checkbox activeCheckbox = new Checkbox("Active");
    final ComboBox<Group> groupComboBox = new ComboBox<>("Group");
    final ComboBox<StoreType> storeTypeComboBox = new ComboBox<>("Authentication");

    private final BeanValidationBinder<User> binder = new BeanValidationBinder<>(User.class);

    private Button saveBtn;
    private Button updateBtn;

    private User userToEdit;
    private boolean editMode = false;

    private final FormUserPresenter presenter;

    public FormUserView(FormUserPresenter presenter) {
        this.presenter = presenter;

        setPadding(true);
        setSpacing(true);
        setMaxWidth("800px");

        nameField.setWidthFull();
        usernameField.setWidthFull();
        emailField.setWidthFull();
        passwordField.setWidthFull();
        passwordConfirmField.setWidthFull();
        groupComboBox.setWidthFull();
        storeTypeComboBox.setWidthFull();

        groupComboBox.setItemLabelGenerator(Group::getName);
        groupComboBox.setItems(presenter.findAllGroups());

        storeTypeComboBox.setItemLabelGenerator(StoreType::name);
        storeTypeComboBox.setItems(StoreType.values());

        binder.forField(nameField).bind("name");
        binder.forField(usernameField).bind("username");
        binder.forField(emailField).bind("email");
        binder.forField(passwordField).bind("password");
        binder.forField(passwordConfirmField).bind("passwordConfirmation");
        binder.forField(activeCheckbox).bind("active");
        binder.forField(groupComboBox).bind("group");
        binder.forField(storeTypeComboBox).bind("storeType");

        var form = new FormLayout(
                nameField, usernameField,
                emailField, activeCheckbox,
                passwordField, passwordConfirmField,
                groupComboBox, storeTypeComboBox
        );
        form.setWidthFull();

        saveBtn = new Button("Save", e -> save());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        updateBtn = new Button("Update", e -> update());
        updateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateBtn.setVisible(false);

        var cancelBtn = new Button("Cancel", e -> UI.getCurrent().navigate("configuration/users"));
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        var toolbar = new HorizontalLayout(saveBtn, updateBtn, cancelBtn);
        toolbar.setSpacing(true);

        add(form, toolbar);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var found = event.getRouteParameters().get("id")
                .map(Long::parseLong)
                .flatMap(presenter::findById);
        if (found.isPresent()) {
            userToEdit = found.get();
            editMode = true;
        } else {
            userToEdit = null;
            editMode = false;
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        if (editMode && userToEdit != null) {
            loadForEdit(userToEdit);
        } else {
            loadForAdd();
        }
    }

    private void loadForEdit(User user) {
        user.setPassword("");
        user.setPasswordConfirmation("");
        binder.setBean(user);
        groupComboBox.setValue(user.getGroup());
        storeTypeComboBox.setValue(user.getStoreType());
        saveBtn.setVisible(false);
        updateBtn.setVisible(true);
    }

    private void loadForAdd() {
        binder.setBean(new User());
        saveBtn.setVisible(true);
        updateBtn.setVisible(false);
    }

    private void save() {
        if (binder.validate().isOk()) {
            try {
                presenter.save(binder.getBean());
                binder.setBean(new User());
                var notification = Notification.show("User saved successfully.");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setDuration(3000);
            } catch (BusinessLogicException ex) {
                var notification = Notification.show(MessageSource.get(ex.getMessage()));
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setDuration(5000);
            }
        }
    }

    private void update() {
        if (binder.validate().isOk()) {
            try {
                presenter.update(binder.getBean());
                var notification = Notification.show("User updated successfully.");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setDuration(3000);
            } catch (BusinessLogicException ex) {
                var notification = Notification.show(MessageSource.get(ex.getMessage()));
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setDuration(5000);
            }
        }
    }
}
