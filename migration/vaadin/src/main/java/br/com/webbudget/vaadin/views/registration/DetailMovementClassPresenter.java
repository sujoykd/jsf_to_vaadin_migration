package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.MovementClass;
import br.com.webbudget.domain.repositories.registration.MovementClassRepository;
import br.com.webbudget.domain.services.MovementClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class DetailMovementClassPresenter {
    private final MovementClassService movementClassService;
    private final MovementClassRepository movementClassRepository;
    public Optional<MovementClass> findById(long id) { return movementClassRepository.findById(id); }
    public void delete(MovementClass mc) { movementClassService.delete(mc); }
}
