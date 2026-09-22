package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.repositories.registration.FinancialPeriodRepository;
import br.com.webbudget.domain.services.ClosingService;
import br.com.webbudget.domain.services.FinancialPeriodService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class DetailFinancialPeriodPresenter {
    private final FinancialPeriodService financialPeriodService;
    private final ClosingService closingService;
    private final FinancialPeriodRepository financialPeriodRepository;
    public Optional<FinancialPeriod> findById(long id) { return financialPeriodRepository.findById(id); }
    public void delete(FinancialPeriod fp) { financialPeriodService.delete(fp); }
    public void reopen(FinancialPeriod fp) { closingService.reopen(fp); }
}
