package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.FixedMovement;
import br.com.webbudget.domain.entities.registration.Contact;
import br.com.webbudget.domain.repositories.financial.FixedMovementRepository;
import br.com.webbudget.domain.repositories.registration.ContactRepository;
import br.com.webbudget.domain.services.FixedMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class FormFixedMovementPresenter {

    private final FixedMovementService fixedMovementService;
    private final FixedMovementRepository fixedMovementRepository;
    private final ContactRepository contactRepository;

    public Optional<FixedMovement> findById(long id) {
        return fixedMovementRepository.findById(id);
    }

    public void save(FixedMovement fixedMovement) {
        fixedMovementService.save(fixedMovement);
    }

    public void update(FixedMovement fixedMovement) {
        fixedMovementService.update(fixedMovement);
    }

    public List<Contact> findContacts(String filter) {
        return contactRepository.findAllBy(filter, true);
    }
}
