package br.com.webbudget.vaadin.views.journal;

import br.com.webbudget.domain.entities.journal.Refueling;
import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.entities.registration.MovementClass;
import br.com.webbudget.domain.entities.registration.MovementClassType;
import br.com.webbudget.domain.entities.registration.Vehicle;
import br.com.webbudget.domain.repositories.journal.RefuelingRepository;
import br.com.webbudget.domain.repositories.registration.FinancialPeriodRepository;
import br.com.webbudget.domain.repositories.registration.MovementClassRepository;
import br.com.webbudget.domain.repositories.registration.VehicleRepository;
import br.com.webbudget.domain.services.RefuelingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class FormRefuelingPresenter {

    private final RefuelingService refuelingService;
    private final RefuelingRepository refuelingRepository;
    private final VehicleRepository vehicleRepository;
    private final FinancialPeriodRepository financialPeriodRepository;
    private final MovementClassRepository movementClassRepository;

    public Optional<Refueling> findById(long id) {
        return refuelingRepository.findById(id);
    }

    public List<Vehicle> findAllVehicles() {
        return vehicleRepository.findAllActive();
    }

    public List<FinancialPeriod> findOpenPeriods() {
        return financialPeriodRepository.findByClosedOrderByIdentificationAsc(false);
    }

    public List<MovementClass> findMovementClassesByVehicle(Vehicle vehicle) {
        if (vehicle == null || vehicle.getCostCenter() == null) {
            return List.of();
        }
        return movementClassRepository.findByMovementClassTypeAndCostCenter(
                MovementClassType.EXPENSE, vehicle.getCostCenter());
    }

    public void save(Refueling refueling, boolean shouldCreateMovement) {
        refuelingService.save(refueling, shouldCreateMovement);
    }
}
