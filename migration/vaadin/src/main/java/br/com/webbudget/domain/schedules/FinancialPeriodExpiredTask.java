package br.com.webbudget.domain.schedules;

import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.repositories.registration.FinancialPeriodRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class FinancialPeriodExpiredTask {

    private final FinancialPeriodRepository financialPeriodRepository;

    public FinancialPeriodExpiredTask(FinancialPeriodRepository financialPeriodRepository) {
        this.financialPeriodRepository = financialPeriodRepository;
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void markAsExpired() {
        final List<FinancialPeriod> periods = this.financialPeriodRepository.findByClosedOrderByIdentificationAsc(false);
        periods.stream()
                .filter(period -> LocalDate.now().compareTo(period.getEnd()) > 0)
                .forEach(period -> {
                    period.setExpired(true);
                    this.financialPeriodRepository.saveAndFlush(period);
                });
    }
}
