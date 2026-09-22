package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.domain.entities.registration.Vehicle;
import br.com.webbudget.domain.repositories.registration.CostCenterRepository;
import br.com.webbudget.domain.repositories.registration.VehicleRepository;
import br.com.webbudget.domain.services.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class FormVehiclePresenter {

    private final VehicleService vehicleService;
    private final VehicleRepository vehicleRepository;
    private final CostCenterRepository costCenterRepository;

    public Optional<Vehicle> findById(long id) {
        return vehicleRepository.findById(id);
    }

    public void save(Vehicle vehicle) {
        vehicleService.save(vehicle);
    }

    public Vehicle update(Vehicle vehicle) {
        return vehicleService.update(vehicle);
    }

    public List<CostCenter> findActiveCostCenters() {
        return costCenterRepository.findAllActive();
    }
}
