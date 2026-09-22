package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.domain.entities.registration.MovementClass;
import br.com.webbudget.domain.repositories.registration.CostCenterRepository;
import br.com.webbudget.domain.repositories.registration.MovementClassRepository;
import br.com.webbudget.domain.services.MovementClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class FormMovementClassPresenter {

    private final MovementClassService movementClassService;
    private final MovementClassRepository movementClassRepository;
    private final CostCenterRepository costCenterRepository;

    public Optional<MovementClass> findById(long id) {
        return movementClassRepository.findById(id);
    }

    public void save(MovementClass mc) {
        movementClassService.save(mc);
    }

    public MovementClass update(MovementClass mc) {
        return movementClassService.update(mc);
    }

    public List<CostCenter> findActiveCostCenters() {
        return costCenterRepository.findAllActive();
    }
}
