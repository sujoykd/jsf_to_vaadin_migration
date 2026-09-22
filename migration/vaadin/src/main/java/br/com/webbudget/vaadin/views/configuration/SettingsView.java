package br.com.webbudget.vaadin.views.configuration;

import br.com.webbudget.domain.entities.configuration.Configuration;
import br.com.webbudget.domain.entities.registration.MovementClass;
import br.com.webbudget.domain.repositories.configuration.ConfigurationRepository;
import br.com.webbudget.domain.repositories.registration.MovementClassRepository;
import br.com.webbudget.domain.services.ConfigurationService;
import br.com.webbudget.vaadin.layout.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "configuration/settings", layout = MainLayout.class)
@PageTitle("Settings")
public class SettingsView extends VerticalLayout implements BeforeEnterObserver {

    final ComboBox<MovementClass> creditCardClassField = new ComboBox<>("Movement Class");
    private final BeanValidationBinder<Configuration> binder = new BeanValidationBinder<>(Configuration.class);
    private Configuration configuration;

    private final ConfigurationService configurationService;
    private final ConfigurationRepository configurationRepository;
    private final MovementClassRepository movementClassRepository;

    public SettingsView(ConfigurationService configurationService,
                        ConfigurationRepository configurationRepository,
                        MovementClassRepository movementClassRepository) {
        this.configurationService = configurationService;
        this.configurationRepository = configurationRepository;
        this.movementClassRepository = movementClassRepository;

        creditCardClassField.setItemLabelGenerator(MovementClass::getName);
        creditCardClassField.setRequiredIndicatorVisible(true);
        binder.forField(creditCardClassField).bind("creditCardClass");

        var form = new FormLayout(creditCardClassField);
        var saveBtn = new Button("Save", e -> save());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        var buttons = new HorizontalLayout(saveBtn);

        add(form, buttons);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.configuration = this.configurationRepository.findCurrent().orElse(new Configuration());
        this.creditCardClassField.setItems(this.movementClassRepository.findAllActive());
        this.binder.readBean(this.configuration);
    }

    private void save() {
        if (binder.writeBeanIfValid(configuration)) {
            configurationService.update(configuration);
            Notification n = Notification.show("Settings saved");
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
    }
}
