package br.com.webbudget.domain.services;

import br.com.webbudget.domain.entities.registration.Contact;
import br.com.webbudget.domain.entities.registration.ContactType;
import br.com.webbudget.domain.entities.registration.Telephone;
import br.com.webbudget.domain.repositories.registration.ContactRepository;
import br.com.webbudget.domain.repositories.registration.TelephoneRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ContactServiceTest {

    @Test
    void save_persists_contact_and_links_telephones() {
        var contact = contact("Alice");
        var telephone = new Telephone();
        telephone.setNumber("99999-1234");
        contact.addTelephone(telephone);

        var contactRepo = mock(ContactRepository.class);
        when(contactRepo.save(contact)).thenReturn(contact);
        var telRepo = mock(TelephoneRepository.class);

        var service = new ContactService(contactRepo, telRepo);
        service.save(contact);

        verify(contactRepo).save(contact);
        verify(telRepo).save(telephone);
        assertThat(telephone.getContact()).isSameAs(contact);
    }

    @Test
    void save_persists_contact_without_telephones() {
        var contact = contact("Bob");
        var contactRepo = mock(ContactRepository.class);
        when(contactRepo.save(contact)).thenReturn(contact);
        var telRepo = mock(TelephoneRepository.class);

        var service = new ContactService(contactRepo, telRepo);
        service.save(contact);

        verify(contactRepo).save(contact);
        verifyNoInteractions(telRepo);
    }

    @Test
    void update_removes_deleted_telephones_and_saves_current_ones() {
        var contact = contact("Carol");
        var telephone = new Telephone();
        telephone.setNumber("88888-0000");
        contact.addTelephone(telephone);

        var toDelete = new Telephone();
        toDelete.setNumber("77777-9999");
        setId(toDelete, 55L);
        contact.removeTelephone(toDelete);

        var contactRepo = mock(ContactRepository.class);
        when(contactRepo.saveAndFlushAndRefresh(contact)).thenReturn(contact);
        var telRepo = mock(TelephoneRepository.class);

        var service = new ContactService(contactRepo, telRepo);
        var result = service.update(contact);

        verify(telRepo).attachAndRemove(toDelete);
        verify(contactRepo).saveAndFlushAndRefresh(contact);
        verify(telRepo).saveAndFlush(telephone);
        assertThat(result).isSameAs(contact);
    }

    @Test
    void delete_removes_contact() {
        var contact = contact("Dave");
        var contactRepo = mock(ContactRepository.class);
        var telRepo = mock(TelephoneRepository.class);

        var service = new ContactService(contactRepo, telRepo);
        service.delete(contact);

        verify(contactRepo).attachAndRemove(contact);
        verifyNoInteractions(telRepo);
    }

    private void setId(Object entity, long id) {
        try {
            var field = entity.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Contact contact(String name) {
        var c = new Contact();
        c.setName(name);
        c.setContactType(ContactType.PERSONAL);
        c.setCity("São Paulo");
        c.setProvince("SP");
        return c;
    }
}
