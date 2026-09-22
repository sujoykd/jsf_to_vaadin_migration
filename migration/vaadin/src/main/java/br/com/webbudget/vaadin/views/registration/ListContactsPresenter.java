package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.registration.Contact;
import br.com.webbudget.domain.repositories.registration.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ListContactsPresenter {

    private final ContactRepository contactRepository;

    public Page<Contact> findAll(String filter, Boolean active, int offset, int limit) {
        return contactRepository.findAllBy(filter, active, offset, limit);
    }

    public int count(String filter, Boolean active) {
        return (int) contactRepository.count(contactRepository.buildSpecification(filter, active));
    }
}
