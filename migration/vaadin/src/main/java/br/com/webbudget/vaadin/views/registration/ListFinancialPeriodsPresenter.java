package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.repositories.registration.FinancialPeriodRepository;
import br.com.webbudget.domain.services.ClosingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ListFinancialPeriodsPresenter {

    private final FinancialPeriodRepository financialPeriodRepository;
    private final ClosingService closingService;

    public Page<FinancialPeriod> findAll(String filter, int offset, int limit) {
        return financialPeriodRepository.findAllBy(filter, null, offset, limit);
    }

    public int count(String filter) {
        return (int) financialPeriodRepository.count(
                financialPeriodRepository.buildSpecification(filter, null));
    }

    public void reopen(FinancialPeriod financialPeriod) {
        closingService.reopen(financialPeriod);
    }
}
