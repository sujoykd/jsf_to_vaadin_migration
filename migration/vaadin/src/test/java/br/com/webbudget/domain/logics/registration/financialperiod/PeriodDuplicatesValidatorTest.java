package br.com.webbudget.domain.logics.registration.financialperiod;

import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.domain.repositories.registration.FinancialPeriodRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PeriodDuplicatesValidatorTest {

    @Test
    void throws_when_identification_already_exists() {
        var existing = period("2024/01");
        var repo = mock(FinancialPeriodRepository.class);
        when(repo.findByIdentification("2024/01")).thenReturn(Optional.of(existing));

        var validator = new PeriodDuplicatesValidator(repo);
        var newPeriod = period("2024/01");

        assertThatThrownBy(() -> validator.run(newPeriod))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessage("error.financial-period.duplicated");
    }

    @Test
    void passes_when_identification_is_unique() {
        var repo = mock(FinancialPeriodRepository.class);
        when(repo.findByIdentification(any())).thenReturn(Optional.empty());

        var validator = new PeriodDuplicatesValidator(repo);
        assertThatCode(() -> validator.run(period("2024/02"))).doesNotThrowAnyException();
    }

    private FinancialPeriod period(String identification) {
        var fp = new FinancialPeriod();
        fp.setIdentification(identification);
        fp.setStart(LocalDate.of(2024, 1, 1));
        fp.setEnd(LocalDate.of(2024, 1, 31));
        return fp;
    }
}
