package br.com.webbudget.domain.logics.registration.financialperiod;

import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.domain.repositories.registration.FinancialPeriodRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PeriodDatesValidatorTest {

    @Test
    void throws_when_dates_collide_with_existing_period() {
        var repo = mock(FinancialPeriodRepository.class);
        when(repo.validatePeriodDates(any(), any())).thenReturn(1L);

        var validator = new PeriodDatesValidator(repo);
        var fp = period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        assertThatThrownBy(() -> validator.run(fp))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessage("error.financial-period.colliding-dates");
    }

    @Test
    void throws_when_start_is_after_end() {
        var repo = mock(FinancialPeriodRepository.class);
        when(repo.validatePeriodDates(any(), any())).thenReturn(0L);

        var validator = new PeriodDatesValidator(repo);
        var fp = period(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 1, 1));

        assertThatThrownBy(() -> validator.run(fp))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessage("error.financial-period.invalid-start-end");
    }

    @Test
    void passes_when_dates_are_valid_and_no_collision() {
        var repo = mock(FinancialPeriodRepository.class);
        when(repo.validatePeriodDates(any(), any())).thenReturn(0L);

        var validator = new PeriodDatesValidator(repo);
        var fp = period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        assertThatCode(() -> validator.run(fp)).doesNotThrowAnyException();
    }

    private FinancialPeriod period(LocalDate start, LocalDate end) {
        var fp = new FinancialPeriod();
        fp.setIdentification("2024/01");
        fp.setStart(start);
        fp.setEnd(end);
        return fp;
    }
}
