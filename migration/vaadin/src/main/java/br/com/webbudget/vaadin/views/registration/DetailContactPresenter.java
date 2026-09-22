package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.Contact;
import br.com.webbudget.domain.repositories.registration.ContactRepository;
import br.com.webbudget.domain.services.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class DetailContactPresenter {
    private final ContactService contactService;
    private final ContactRepository contactRepository;
    public Optional<Contact> findById(long id) { return contactRepository.findById(id); }
    public void delete(Contact contact) { contactService.delete(contact); }
}
