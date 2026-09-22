package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.FixedMovement;
import br.com.webbudget.domain.entities.financial.Launch;
import br.com.webbudget.domain.repositories.financial.FixedMovementRepository;
import br.com.webbudget.domain.repositories.financial.LaunchRepository;
import br.com.webbudget.domain.services.FixedMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class DetailFixedMovementPresenter {

    private final FixedMovementRepository fixedMovementRepository;
    private final FixedMovementService fixedMovementService;
    private final LaunchRepository launchRepository;

    public Optional<FixedMovement> findById(long id) {
        return fixedMovementRepository.findById(id);
    }

    public void delete(FixedMovement fixedMovement) {
        fixedMovementService.delete(fixedMovement);
    }

    public List<Launch> findLaunches(FixedMovement fixedMovement) {
        return launchRepository.findByFixedMovement(fixedMovement);
    }
}
