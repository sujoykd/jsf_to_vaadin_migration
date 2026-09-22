package br.com.webbudget.vaadin.views.configuration;

import br.com.webbudget.domain.entities.configuration.Authorization;
import br.com.webbudget.domain.entities.configuration.Group;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.infrastructure.i18n.MessageSource;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.List;

@PermitAll
@PageTitle("Groups")
@Route(value = "configuration/groups/form/:id?", layout = MainLayout.class)
public class FormGroupView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    final TextField nameField = new TextField("Name");
    final Checkbox activeCheckbox = new Checkbox("Active");
    final ComboBox<Group> parentComboBox = new ComboBox<>("Parent Group");
    final Grid<Authorization> permissionsGrid = new Grid<>(Authorization.class, false);

    private final FormGroupPresenter presenter;
    private final BeanValidationBinder<Group> binder = new BeanValidationBinder<>(Group.class);

    private boolean editMode = false;
    private Group currentGroup;

    private Button saveBtn;
    private Button updateBtn;

    public FormGroupView(FormGroupPresenter presenter) {
        this.presenter = presenter;

        setPadding(true);
        setSpacing(true);
        setMaxWidth("800px");

        nameField.setRequired(true);

        parentComboBox.setItemLabelGenerator(Group::getName);
        parentComboBox.setItems(presenter.findAllGroups());
        parentComboBox.setClearButtonVisible(true);

        binder.forField(nameField).bind(Group::getName, Group::setName);
        binder.forField(activeCheckbox).bind(Group::isActive, Group::setActive);

        var formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.add(nameField, activeCheckbox, parentComboBox);
        formLayout.setColspan(parentComboBox, 2);

        permissionsGrid.addColumn(Authorization::getFunctionality).setHeader("Functionality");
        permissionsGrid.addColumn(Authorization::getPermission).setHeader("Permission");
        permissionsGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        permissionsGrid.setHeight("300px");
        permissionsGrid.setItems(presenter.loadAllAuthorizations());

        saveBtn = new Button("Save");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> handleSave());

        updateBtn = new Button("Update");
        updateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateBtn.addClickListener(e -> handleUpdate());
        updateBtn.setVisible(false);

        var backBtn = new Button("Back");
        backBtn.addClickListener(e -> UI.getCurrent().navigate("configuration/groups"));

        var toolbar = new HorizontalLayout(saveBtn, updateBtn, backBtn);
        toolbar.setSpacing(true);

        add(formLayout, permissionsGrid, toolbar);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var idParam = event.getRouteParameters().get("id");
        if (idParam.isPresent() && !idParam.get().isBlank()) {
            try {
                long id = Long.parseLong(idParam.get());
                var optGroup = presenter.findById(id);
                if (optGroup.isPresent()) {
                    currentGroup = optGroup.get();
                    editMode = true;
                } else {
                    currentGroup = null;
                    editMode = false;
                }
            } catch (NumberFormatException e) {
                currentGroup = null;
                editMode = false;
            }
        } else {
            currentGroup = null;
            editMode = false;
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        if (editMode && currentGroup != null) {
            binder.setBean(currentGroup);
            parentComboBox.setValue(currentGroup.getParent());
            selectCurrentPermissions(currentGroup);
            saveBtn.setVisible(false);
            updateBtn.setVisible(true);
        } else {
            initAddMode();
        }
    }

    private void initAddMode() {
        editMode = false;
        currentGroup = null;
        binder.setBean(new Group());
        parentComboBox.clear();
        permissionsGrid.asMultiSelect().clear();
        saveBtn.setVisible(true);
        updateBtn.setVisible(false);
    }

    private void selectCurrentPermissions(Group group) {
        var currentPermissions = group.getPermissions();
        var allItems = permissionsGrid.getListDataView().getItems().toList();
        var toSelect = allItems.stream()
                .filter(auth -> currentPermissions.contains(auth.getFullPermission()))
                .toList();
        permissionsGrid.asMultiSelect().select(toSelect);
    }

    private void handleSave() {
        if (!binder.validate().isOk()) {
            return;
        }
        var group = binder.getBean();
        group.setParent(parentComboBox.getValue());
        var selectedAuthorizations = new ArrayList<>(permissionsGrid.asMultiSelect().getSelectedItems());
        try {
            presenter.save(group, selectedAuthorizations);
            initAddMode();
            Notification.show("Group saved successfully").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (BusinessLogicException ex) {
            var notification = Notification.show(MessageSource.get(ex.getMessage()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        }
    }

    private void handleUpdate() {
        if (!binder.validate().isOk()) {
            return;
        }
        var group = binder.getBean();
        group.setParent(parentComboBox.getValue());
        var selectedAuthorizations = new ArrayList<>(permissionsGrid.asMultiSelect().getSelectedItems());
        try {
            presenter.update(group, selectedAuthorizations);
            Notification.show("Group updated successfully").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate("configuration/groups");
        } catch (BusinessLogicException ex) {
            var notification = Notification.show(MessageSource.get(ex.getMessage()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        }
    }
}
