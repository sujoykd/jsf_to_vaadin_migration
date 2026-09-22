package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.domain.repositories.registration.CostCenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ListCostCentersPresenter {

    private final CostCenterRepository costCenterRepository;

    public Page<CostCenter> findAll(String filter, Boolean active, int offset, int limit) {
        return costCenterRepository.findAllBy(filter, active, offset, limit);
    }

    public int count(String filter, Boolean active) {
        return (int) costCenterRepository.count(costCenterRepository.buildSpecification(filter, active));
    }
}
