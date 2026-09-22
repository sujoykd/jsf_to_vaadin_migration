package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.PeriodMovement;
import br.com.webbudget.domain.entities.registration.Contact;
import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.repositories.financial.PeriodMovementRepository;
import br.com.webbudget.domain.repositories.registration.ContactRepository;
import br.com.webbudget.domain.repositories.registration.FinancialPeriodRepository;
import br.com.webbudget.domain.services.PeriodMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class FormPeriodMovementPresenter {

    private final PeriodMovementService periodMovementService;
    private final PeriodMovementRepository periodMovementRepository;
    private final FinancialPeriodRepository financialPeriodRepository;
    private final ContactRepository contactRepository;

    public Optional<PeriodMovement> findById(long id) {
        return periodMovementRepository.findById(id);
    }

    public PeriodMovement save(PeriodMovement pm) {
        return periodMovementService.save(pm);
    }

    public PeriodMovement update(PeriodMovement pm) {
        return periodMovementService.update(pm);
    }

    public List<FinancialPeriod> findOpenPeriods() {
        return financialPeriodRepository.findByClosedOrderByIdentificationAsc(false);
    }

    public List<Contact> findActiveContacts() {
        return contactRepository.findAllBy(null, true);
    }
}
