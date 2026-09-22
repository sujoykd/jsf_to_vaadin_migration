package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.registration.Vehicle;
import br.com.webbudget.domain.repositories.registration.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ListVehiclesPresenter {

    private final VehicleRepository vehicleRepository;

    public Page<Vehicle> findAll(String filter, Boolean active, int offset, int limit) {
        return vehicleRepository.findAllBy(filter, active, offset, limit);
    }

    public int count(String filter, Boolean active) {
        return (int) vehicleRepository.count(vehicleRepository.buildSpecification(filter, active));
    }
}
