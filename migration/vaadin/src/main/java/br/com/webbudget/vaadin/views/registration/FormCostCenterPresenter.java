package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.domain.repositories.registration.CostCenterRepository;
import br.com.webbudget.domain.services.CostCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class FormCostCenterPresenter {

    private final CostCenterService costCenterService;
    private final CostCenterRepository costCenterRepository;

    public Optional<CostCenter> findById(long id) {
        return costCenterRepository.findById(id);
    }

    public void save(CostCenter cc) {
        costCenterService.save(cc);
    }

    public CostCenter update(CostCenter cc) {
        return costCenterService.update(cc);
    }

    public List<CostCenter> findAllActive() {
        return costCenterRepository.findAllActive();
    }
}
