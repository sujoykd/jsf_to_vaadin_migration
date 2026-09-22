package br.com.webbudget.vaadin.views.journal;

import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.journal.Refueling;
import br.com.webbudget.domain.repositories.journal.RefuelingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ListRefuelingsPresenter {

    private final RefuelingRepository refuelingRepository;

    public Page<Refueling> findAll(String filter, Boolean active, int offset, int limit) {
        return refuelingRepository.findAllBy(filter, active, offset, limit);
    }

    public int count(String filter, Boolean active) {
        return (int) refuelingRepository.count(refuelingRepository.buildSpecification(filter, active));
    }
}
