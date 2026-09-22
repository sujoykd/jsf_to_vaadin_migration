package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.application.components.ui.filter.PeriodMovementFilter;
import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.financial.PeriodMovement;
import br.com.webbudget.domain.repositories.financial.PeriodMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ListPeriodMovementsPresenter {

    private final PeriodMovementRepository periodMovementRepository;

    public Page<PeriodMovement> findAll(String filter, int offset, int limit) {
        var f = new PeriodMovementFilter();
        f.setValue(filter);
        return periodMovementRepository.findAllBy(f, offset, limit);
    }

    public int count(String filter) {
        var f = new PeriodMovementFilter();
        f.setValue(filter);
        return (int) periodMovementRepository.count(periodMovementRepository.buildSpecification(f));
    }
}
