package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.domain.repositories.registration.CostCenterRepository;
import br.com.webbudget.domain.services.CostCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class DetailCostCenterPresenter {
    private final CostCenterService costCenterService;
    private final CostCenterRepository costCenterRepository;
    public Optional<CostCenter> findById(long id) { return costCenterRepository.findById(id); }
    public void delete(CostCenter costCenter) { costCenterService.delete(costCenter); }
}
