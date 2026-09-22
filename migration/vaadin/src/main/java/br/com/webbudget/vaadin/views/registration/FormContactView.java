package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.Contact;
import br.com.webbudget.domain.entities.registration.ContactType;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.infrastructure.i18n.MessageSource;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "registration/contacts/form/:id?", layout = MainLayout.class)
@PageTitle("Contacts")
public class FormContactView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    final Checkbox activeCheckbox = new Checkbox("Active");
    final TextField nameField = new TextField("Name");
    final DatePicker birthDatePicker = new DatePicker("Birth Date");
    final Select<ContactType> contactTypeSelect = new Select<>();
    final TextField documentField = new TextField("Document");
    final TextField emailField = new TextField("E-mail");
    final TextArea otherInformationArea = new TextArea("Other Information");
    final TextField zipcodeField = new TextField("ZIP Code");
    final TextField streetField = new TextField("Street");
    final TextField streetNumber = new TextField("Number");
    final TextField complementField = new TextField("Complement");
    final TextField neighborhoodField = new TextField("Neighborhood");
    final TextField cityField = new TextField("City");
    final TextField provinceField = new TextField("State");

    Button saveButton = new Button("Save");
    Button updateButton = new Button("Update");
    Button backButton = new Button("Back");

    private boolean editMode = false;
    private Contact currentContact;

    private final FormContactPresenter presenter;

    public FormContactView(FormContactPresenter presenter) {
        this.presenter = presenter;

        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");

        contactTypeSelect.setLabel("Type");
        contactTypeSelect.setItems(ContactType.values());
        contactTypeSelect.setItemLabelGenerator(ct -> ct != null ? MessageSource.get(ct.toString()) : "");
        contactTypeSelect.setWidthFull();

        nameField.setWidthFull();
        nameField.setRequired(true);
        nameField.setRequiredIndicatorVisible(true);

        birthDatePicker.setWidthFull();
        documentField.setWidthFull();
        emailField.setWidthFull();
        otherInformationArea.setWidthFull();
        otherInformationArea.setMinHeight("80px");
        zipcodeField.setWidthFull();
        streetField.setWidthFull();
        streetNumber.setWidthFull();
        complementField.setWidthFull();
        neighborhoodField.setWidthFull();
        cityField.setWidthFull();
        provinceField.setWidthFull();

        var formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        formLayout.add(activeCheckbox, nameField);
        formLayout.add(contactTypeSelect, birthDatePicker);
        formLayout.add(documentField, emailField);
        formLayout.add(otherInformationArea, 2);
        formLayout.add(zipcodeField, streetField);
        formLayout.add(streetNumber, complementField);
        formLayout.add(neighborhoodField, cityField);
        formLayout.add(provinceField);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> onSave());

        updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateButton.addClickListener(e -> onUpdate());
        updateButton.setVisible(false);

        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("registration/contacts"));

        var toolbar = new HorizontalLayout(saveButton, updateButton, backButton);
        toolbar.setSpacing(true);

        add(formLayout, toolbar);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var idParam = event.getRouteParameters().get("id");
        if (idParam.isPresent()) {
            long id = Long.parseLong(idParam.get());
            presenter.findById(id).ifPresentOrElse(contact -> {
                currentContact = contact;
                editMode = true;
            }, () -> event.forwardTo("registration/contacts"));
        } else {
            currentContact = new Contact();
            editMode = false;
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        populateForm(currentContact);
        saveButton.setVisible(!editMode);
        updateButton.setVisible(editMode);
    }

    private void populateForm(Contact contact) {
        activeCheckbox.setValue(contact.isActive());
        nameField.setValue(contact.getName() != null ? contact.getName() : "");
        birthDatePicker.setValue(contact.getBirthDate());
        contactTypeSelect.setValue(contact.getContactType());
        documentField.setValue(contact.getDocument() != null ? contact.getDocument() : "");
        emailField.setValue(contact.getEmail() != null ? contact.getEmail() : "");
        otherInformationArea.setValue(contact.getOtherInformation() != null ? contact.getOtherInformation() : "");
        zipcodeField.setValue(contact.getZipcode() != null ? contact.getZipcode() : "");
        streetField.setValue(contact.getStreet() != null ? contact.getStreet() : "");
        streetNumber.setValue(contact.getNumber() != null ? contact.getNumber() : "");
        complementField.setValue(contact.getComplement() != null ? contact.getComplement() : "");
        neighborhoodField.setValue(contact.getNeighborhood() != null ? contact.getNeighborhood() : "");
        cityField.setValue(contact.getCity() != null ? contact.getCity() : "");
        provinceField.setValue(contact.getProvince() != null ? contact.getProvince() : "");
    }

    private boolean validateFields() {
        boolean valid = true;
        if (nameField.getValue() == null || nameField.getValue().isBlank()) {
            nameField.setInvalid(true);
            nameField.setErrorMessage("Name is required");
            valid = false;
        } else {
            nameField.setInvalid(false);
        }
        if (contactTypeSelect.getValue() == null) {
            contactTypeSelect.setInvalid(true);
            contactTypeSelect.setErrorMessage("Type is required");
            valid = false;
        } else {
            contactTypeSelect.setInvalid(false);
        }
        if (cityField.getValue() == null || cityField.getValue().isBlank()) {
            cityField.setInvalid(true);
            cityField.setErrorMessage("City is required");
            valid = false;
        } else {
            cityField.setInvalid(false);
        }
        if (provinceField.getValue() == null || provinceField.getValue().isBlank()) {
            provinceField.setInvalid(true);
            provinceField.setErrorMessage("State is required");
            valid = false;
        } else {
            provinceField.setInvalid(false);
        }
        return valid;
    }

    private Contact buildEntity() {
        if (currentContact == null) {
            currentContact = new Contact();
        }
        currentContact.setActive(activeCheckbox.getValue());
        currentContact.setName(nameField.getValue());
        currentContact.setBirthDate(birthDatePicker.getValue());
        currentContact.setContactType(contactTypeSelect.getValue());
        currentContact.setDocument(documentField.getValue());
        currentContact.setEmail(emailField.getValue());
        currentContact.setOtherInformation(otherInformationArea.getValue());
        currentContact.setZipcode(zipcodeField.getValue());
        currentContact.setStreet(streetField.getValue());
        currentContact.setNumber(streetNumber.getValue());
        currentContact.setComplement(complementField.getValue());
        currentContact.setNeighborhood(neighborhoodField.getValue());
        currentContact.setCity(cityField.getValue());
        currentContact.setProvince(provinceField.getValue());
        return currentContact;
    }

    private void onSave() {
        if (!validateFields()) {
            return;
        }
        try {
            presenter.save(buildEntity());
            currentContact = new Contact();
            populateForm(currentContact);
            var notification = Notification.show("Contact saved successfully.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(3000);
        } catch (BusinessLogicException ex) {
            var notification = Notification.show(MessageSource.get(ex.getMessage()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        } catch (RuntimeException ex) {
            var notification = Notification.show("Error saving contact: " + ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        }
    }

    private void onUpdate() {
        if (!validateFields()) {
            return;
        }
        try {
            currentContact = presenter.update(buildEntity());
            var notification = Notification.show("Contact updated successfully.");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(3000);
        } catch (BusinessLogicException ex) {
            var notification = Notification.show(MessageSource.get(ex.getMessage()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        } catch (RuntimeException ex) {
            var notification = Notification.show("Error updating contact: " + ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setDuration(5000);
        }
    }
}
