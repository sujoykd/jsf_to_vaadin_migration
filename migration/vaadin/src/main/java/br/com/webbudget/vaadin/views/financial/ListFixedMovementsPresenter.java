package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.application.components.ui.filter.FixedMovementFilter;
import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.financial.FixedMovement;
import br.com.webbudget.domain.entities.financial.FixedMovementState;
import br.com.webbudget.domain.repositories.financial.FixedMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ListFixedMovementsPresenter {

    private final FixedMovementRepository fixedMovementRepository;

    public Page<FixedMovement> findAll(String filter, FixedMovementState state, int offset, int limit) {
        FixedMovementFilter fixedMovementFilter = new FixedMovementFilter();
        fixedMovementFilter.setValue(filter);
        fixedMovementFilter.setFixedMovementState(state);
        return fixedMovementRepository.findAllBy(fixedMovementFilter, offset, limit);
    }

    public int count(String filter, FixedMovementState state) {
        FixedMovementFilter fixedMovementFilter = new FixedMovementFilter();
        fixedMovementFilter.setValue(filter);
        fixedMovementFilter.setFixedMovementState(state);
        return (int) fixedMovementRepository.count(
                fixedMovementRepository.buildSpecification(fixedMovementFilter));
    }
}
