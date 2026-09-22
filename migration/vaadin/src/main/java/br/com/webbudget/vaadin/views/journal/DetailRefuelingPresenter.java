package br.com.webbudget.vaadin.views.journal;

import br.com.webbudget.domain.entities.journal.Refueling;
import br.com.webbudget.domain.repositories.journal.RefuelingRepository;
import br.com.webbudget.domain.services.RefuelingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class DetailRefuelingPresenter {

    private final RefuelingService refuelingService;
    private final RefuelingRepository refuelingRepository;

    public Optional<Refueling> findById(long id) {
        return refuelingRepository.findById(id);
    }

    public void delete(Refueling refueling) {
        refuelingService.delete(refueling);
    }

    public void createFinancialMovement(Refueling refueling) {
        refuelingService.createFinancialMovement(refueling);
    }
}
