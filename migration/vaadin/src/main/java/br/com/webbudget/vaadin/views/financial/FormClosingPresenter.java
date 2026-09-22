package br.com.webbudget.vaadin.views.financial;

import br.com.webbudget.domain.entities.financial.Closing;
import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.repositories.registration.FinancialPeriodRepository;
import br.com.webbudget.domain.services.ClosingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class FormClosingPresenter {

    private final ClosingService closingService;
    private final FinancialPeriodRepository financialPeriodRepository;

    public List<FinancialPeriod> loadOpenPeriods() {
        return financialPeriodRepository.findByClosedOrderByIdentificationAsc(false);
    }

    public Closing simulate(FinancialPeriod financialPeriod) {
        return closingService.simulate(financialPeriod);
    }

    public void close(FinancialPeriod financialPeriod) {
        closingService.close(financialPeriod);
    }
}
