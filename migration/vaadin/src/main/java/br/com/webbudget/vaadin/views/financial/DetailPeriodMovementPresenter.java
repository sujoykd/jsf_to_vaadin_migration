package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.PeriodMovement;
import br.com.webbudget.domain.repositories.financial.PeriodMovementRepository;
import br.com.webbudget.domain.services.PeriodMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class DetailPeriodMovementPresenter {

    private final PeriodMovementService periodMovementService;
    private final PeriodMovementRepository periodMovementRepository;

    public Optional<PeriodMovement> findById(long id) {
        return periodMovementRepository.findById(id);
    }

    public void delete(PeriodMovement periodMovement) {
        periodMovementService.delete(periodMovement);
    }
}
