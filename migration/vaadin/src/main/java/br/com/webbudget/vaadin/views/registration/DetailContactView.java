package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.Contact;
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
@Route(value = "registration/contacts/detail/:id", layout = MainLayout.class)
@PageTitle("Contacts")
public class DetailContactView extends VerticalLayout implements BeforeEnterObserver {

    final Checkbox activeCheckbox = new Checkbox("Active");
    final TextField nameField = new TextField("Name");
    final TextField birthDateField = new TextField("Birth Date");
    final TextField typeField = new TextField("Type");
    final TextField documentField = new TextField("Document");
    final TextField emailField = new TextField("E-mail");
    final TextArea otherInfoArea = new TextArea("Other Information");
    final TextField zipcodeField = new TextField("ZIP Code");
    final TextField streetField = new TextField("Street");
    final TextField numberField = new TextField("Number");
    final TextField complementField = new TextField("Complement");
    final TextField neighborhoodField = new TextField("Neighborhood");
    final TextField cityField = new TextField("City");
    final TextField stateField = new TextField("State");
    final Button editButton = new Button("Edit");
    final Button deleteButton = new Button("Delete");
    final Button backButton = new Button("Back");

    private Contact currentContact;
    private final DetailContactPresenter presenter;

    public DetailContactView(DetailContactPresenter presenter) {
        this.presenter = presenter;
        setPadding(true); setSpacing(true); setMaxWidth("900px");
        activeCheckbox.setEnabled(false);
        for (var f : new TextField[]{nameField, birthDateField, typeField, documentField, emailField,
                zipcodeField, streetField, numberField, complementField, neighborhoodField, cityField, stateField}) {
            f.setReadOnly(true); f.setWidthFull();
        }
        otherInfoArea.setReadOnly(true); otherInfoArea.setWidthFull();

        var formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));
        formLayout.add(activeCheckbox);
        formLayout.add(nameField, birthDateField);
        formLayout.add(typeField, documentField);
        formLayout.add(emailField);
        formLayout.add(otherInfoArea, 2);
        formLayout.add(zipcodeField, streetField);
        formLayout.add(numberField, complementField);
        formLayout.add(neighborhoodField, cityField);
        formLayout.add(stateField);

        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e -> { if (currentContact != null) UI.getCurrent().navigate(FormContactView.class, new RouteParameters("id", String.valueOf(currentContact.getId()))); });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> handleDelete());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("registration/contacts"));

        add(formLayout, new HorizontalLayout(editButton, deleteButton, backButton));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getRouteParameters().get("id").map(Long::parseLong).flatMap(presenter::findById)
                .ifPresentOrElse(this::loadContact, () -> event.forwardTo("registration/contacts"));
    }

    private void loadContact(Contact contact) {
        currentContact = contact;
        activeCheckbox.setValue(contact.isActive());
        nameField.setValue(contact.getName() != null ? contact.getName() : "");
        birthDateField.setValue(contact.getBirthDate() != null ? contact.getBirthDate().toString() : "");
        typeField.setValue(contact.getContactType() != null ? contact.getContactType().name() : "");
        documentField.setValue(contact.getDocument() != null ? contact.getDocument() : "");
        emailField.setValue(contact.getEmail() != null ? contact.getEmail() : "");
        otherInfoArea.setValue(contact.getOtherInformation() != null ? contact.getOtherInformation() : "");
        zipcodeField.setValue(contact.getZipcode() != null ? contact.getZipcode() : "");
        streetField.setValue(contact.getStreet() != null ? contact.getStreet() : "");
        numberField.setValue(contact.getNumber() != null ? contact.getNumber() : "");
        complementField.setValue(contact.getComplement() != null ? contact.getComplement() : "");
        neighborhoodField.setValue(contact.getNeighborhood() != null ? contact.getNeighborhood() : "");
        cityField.setValue(contact.getCity() != null ? contact.getCity() : "");
        stateField.setValue(contact.getProvince() != null ? contact.getProvince() : "");
    }

    private void handleDelete() {
        if (currentContact == null) return;
        var dialog = new ConfirmDialog();
        dialog.setHeader("Confirm Delete");
        dialog.setText("Delete contact '" + currentContact.getName() + "'?");
        dialog.setCancelable(true); dialog.setCancelText("No"); dialog.setConfirmText("Yes");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(ev -> { presenter.delete(currentContact); UI.getCurrent().navigate("registration/contacts"); });
        dialog.open();
    }
}
