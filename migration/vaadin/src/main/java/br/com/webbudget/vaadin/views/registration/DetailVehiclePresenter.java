package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.Vehicle;
import br.com.webbudget.domain.repositories.registration.VehicleRepository;
import br.com.webbudget.domain.services.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class DetailVehiclePresenter {
    private final VehicleService vehicleService;
    private final VehicleRepository vehicleRepository;
    public Optional<Vehicle> findById(long id) { return vehicleRepository.findById(id); }
    public void delete(Vehicle vehicle) { vehicleService.delete(vehicle); }
}
