package br.com.webbudget.domain.services;

import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.events.FinancialPeriodOpenedEvent;
import br.com.webbudget.domain.logics.registration.financialperiod.PeriodDeletingLogic;
import br.com.webbudget.domain.logics.registration.financialperiod.PeriodSavingLogic;
import br.com.webbudget.domain.repositories.registration.FinancialPeriodRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FinancialPeriodServiceTest {

    @Test
    void save_runs_saving_logics_and_publishes_event() {
        var fp = period("2024/01", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        var repo = mock(FinancialPeriodRepository.class);
        when(repo.save(fp)).thenReturn(fp);
        var savingLogic = mock(PeriodSavingLogic.class);
        var publisher = mock(ApplicationEventPublisher.class);

        var service = new FinancialPeriodService(repo, publisher, List.of(savingLogic), List.of());
        service.save(fp);

        verify(savingLogic).run(fp);
        verify(repo).save(fp);
        var captor = ArgumentCaptor.forClass(FinancialPeriodOpenedEvent.class);
        verify(publisher).publishEvent(captor.capture());
        assertThat(captor.getValue().financialPeriod()).isSameAs(fp);
    }

    @Test
    void save_marks_period_as_expired_when_end_is_in_the_past() {
        var past = LocalDate.now().minusDays(30);
        var fp = period("2023/12", past.minusDays(30), past);
        var repo = mock(FinancialPeriodRepository.class);
        when(repo.save(fp)).thenReturn(fp);
        var publisher = mock(ApplicationEventPublisher.class);

        var service = new FinancialPeriodService(repo, publisher, List.of(), List.of());
        service.save(fp);

        assertThat(fp.isExpired()).isTrue();
    }

    @Test
    void save_does_not_mark_period_as_expired_when_end_is_in_future() {
        var fp = period("2025/01", LocalDate.now(), LocalDate.now().plusDays(30));
        var repo = mock(FinancialPeriodRepository.class);
        when(repo.save(fp)).thenReturn(fp);
        var publisher = mock(ApplicationEventPublisher.class);

        var service = new FinancialPeriodService(repo, publisher, List.of(), List.of());
        service.save(fp);

        assertThat(fp.isExpired()).isFalse();
    }

    @Test
    void delete_runs_deleting_logics_and_removes() {
        var fp = period("2024/01", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        var repo = mock(FinancialPeriodRepository.class);
        var deletingLogic = mock(PeriodDeletingLogic.class);
        var publisher = mock(ApplicationEventPublisher.class);

        var service = new FinancialPeriodService(repo, publisher, List.of(), List.of(deletingLogic));
        service.delete(fp);

        verify(deletingLogic).run(fp);
        verify(repo).attachAndRemove(fp);
    }

    private FinancialPeriod period(String id, LocalDate start, LocalDate end) {
        var fp = new FinancialPeriod();
        fp.setIdentification(id);
        fp.setStart(start);
        fp.setEnd(end);
        fp.setCreditCardGoal(BigDecimal.ZERO);
        fp.setExpensesGoal(BigDecimal.ZERO);
        fp.setRevenuesGoal(BigDecimal.ZERO);
        return fp;
    }
}
