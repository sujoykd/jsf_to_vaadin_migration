package br.com.webbudget.domain.logics.registration.movementclass;

import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.domain.entities.registration.MovementClass;
import br.com.webbudget.domain.entities.registration.MovementClassType;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.domain.repositories.registration.MovementClassRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MovementClassBudgetValidatorTest {

    @Test
    void passes_when_cost_center_does_not_control_budget() {
        var costCenter = costCenter("Household", BigDecimal.ZERO, BigDecimal.ZERO);
        var mc = movementClass("Groceries", MovementClassType.EXPENSE, costCenter, new BigDecimal("100"));
        var repo = mock(MovementClassRepository.class);

        var validator = new MovementClassBudgetValidator(repo);
        assertThatCode(() -> validator.run(mc)).doesNotThrowAnyException();
        verifyNoInteractions(repo);
    }

    @Test
    void passes_when_enough_budget_is_available() {
        var costCenter = costCenter("Household", new BigDecimal("500"), BigDecimal.ZERO);
        var mc = movementClass("Groceries", MovementClassType.EXPENSE, costCenter, new BigDecimal("200"));
        var repo = mock(MovementClassRepository.class);
        when(repo.findByMovementClassTypeAndCostCenter(MovementClassType.EXPENSE, costCenter))
                .thenReturn(List.of());

        var validator = new MovementClassBudgetValidator(repo);
        assertThatCode(() -> validator.run(mc)).doesNotThrowAnyException();
    }

    @Test
    void throws_when_budget_is_exceeded() {
        var costCenter = costCenter("Household", new BigDecimal("100"), BigDecimal.ZERO);
        var existing = movementClass("Rent", MovementClassType.EXPENSE, costCenter, new BigDecimal("80"));
        var mc = movementClass("Groceries", MovementClassType.EXPENSE, costCenter, new BigDecimal("50"));

        var repo = mock(MovementClassRepository.class);
        when(repo.findByMovementClassTypeAndCostCenter(MovementClassType.EXPENSE, costCenter))
                .thenReturn(List.of(existing));

        var validator = new MovementClassBudgetValidator(repo);
        assertThatThrownBy(() -> validator.run(mc))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("error.movement-class.no-budget");
    }

    @Test
    void passes_when_enough_revenue_budget_available() {
        var costCenter = costCenter("Household", BigDecimal.ZERO, new BigDecimal("1000"));
        var mc = movementClass("Salary", MovementClassType.REVENUE, costCenter, new BigDecimal("800"));
        var repo = mock(MovementClassRepository.class);
        when(repo.findByMovementClassTypeAndCostCenter(MovementClassType.REVENUE, costCenter))
                .thenReturn(List.of());

        var validator = new MovementClassBudgetValidator(repo);
        assertThatCode(() -> validator.run(mc)).doesNotThrowAnyException();
    }

    private CostCenter costCenter(String name, BigDecimal expensesBudget, BigDecimal revenuesBudget) {
        var cc = new CostCenter();
        cc.setName(name);
        cc.setExpensesBudget(expensesBudget);
        cc.setRevenuesBudget(revenuesBudget);
        return cc;
    }

    private MovementClass movementClass(String name, MovementClassType type, CostCenter costCenter, BigDecimal budget) {
        var mc = new MovementClass();
        mc.setName(name);
        mc.setMovementClassType(type);
        mc.setCostCenter(costCenter);
        mc.setBudget(budget);
        return mc;
    }
}
