package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.registration.MovementClass;
import br.com.webbudget.domain.repositories.registration.MovementClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ListMovementClassesPresenter {

    private final MovementClassRepository movementClassRepository;

    public Page<MovementClass> findAll(String filter, Boolean active, int offset, int limit) {
        return movementClassRepository.findAllBy(filter, active, offset, limit);
    }

    public int count(String filter, Boolean active) {
        return (int) movementClassRepository.count(movementClassRepository.buildSpecification(filter, active));
    }
}
