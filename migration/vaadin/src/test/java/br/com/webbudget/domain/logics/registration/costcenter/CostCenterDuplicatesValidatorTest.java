package br.com.webbudget.domain.logics.registration.costcenter;

import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.domain.repositories.registration.CostCenterRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CostCenterDuplicatesValidatorTest {

    @Test
    void throws_when_name_already_exists() {
        var existing = costCenter("Household");
        var repo = mock(CostCenterRepository.class);
        when(repo.findByName("Household")).thenReturn(Optional.of(existing));

        var validator = new CostCenterDuplicatesValidator(repo);

        assertThatThrownBy(() -> validator.run(costCenter("Household")))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessage("error.cost-center.duplicated");
    }

    @Test
    void passes_when_name_is_unique() {
        var repo = mock(CostCenterRepository.class);
        when(repo.findByName(any())).thenReturn(Optional.empty());

        var validator = new CostCenterDuplicatesValidator(repo);
        assertThatCode(() -> validator.run(costCenter("Transport"))).doesNotThrowAnyException();
    }

    private CostCenter costCenter(String name) {
        var cc = new CostCenter();
        cc.setName(name);
        cc.setExpensesBudget(BigDecimal.ZERO);
        cc.setRevenuesBudget(BigDecimal.ZERO);
        return cc;
    }
}
