package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.repositories.registration.FinancialPeriodRepository;
import br.com.webbudget.domain.services.FinancialPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class FormFinancialPeriodPresenter {

    private final FinancialPeriodService financialPeriodService;
    private final FinancialPeriodRepository financialPeriodRepository;

    public Optional<FinancialPeriod> findById(long id) {
        return financialPeriodRepository.findById(id);
    }

    public void save(FinancialPeriod fp) {
        financialPeriodService.save(fp);
    }
}
