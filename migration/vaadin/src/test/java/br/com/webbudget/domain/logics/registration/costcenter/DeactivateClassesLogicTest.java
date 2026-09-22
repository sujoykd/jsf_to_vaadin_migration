package br.com.webbudget.domain.logics.registration.costcenter;

import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.domain.entities.registration.MovementClass;
import br.com.webbudget.domain.entities.registration.MovementClassType;
import br.com.webbudget.domain.repositories.registration.MovementClassRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

class DeactivateClassesLogicTest {

    @Test
    void deactivates_all_active_classes_when_cost_center_is_deactivated() {
        var costCenter = costCenter("Household", false);
        var mc1 = movementClass("Groceries", MovementClassType.EXPENSE, costCenter);
        var mc2 = movementClass("Rent", MovementClassType.EXPENSE, costCenter);
        var repo = mock(MovementClassRepository.class);
        when(repo.findByActiveAndCostCenterOrderByNameAsc(true, costCenter)).thenReturn(List.of(mc1, mc2));

        var logic = new DeactivateClassesLogic(repo);
        logic.run(costCenter);

        assertThat(mc1.isActive()).isFalse();
        assertThat(mc2.isActive()).isFalse();
        verify(repo, times(2)).saveAndFlushAndRefresh(any());
    }

    @Test
    void does_nothing_when_cost_center_is_still_active() {
        var costCenter = costCenter("Household", true);
        var repo = mock(MovementClassRepository.class);

        var logic = new DeactivateClassesLogic(repo);
        assertThatCode(() -> logic.run(costCenter)).doesNotThrowAnyException();
        verifyNoInteractions(repo);
    }

    private CostCenter costCenter(String name, boolean active) {
        var cc = new CostCenter();
        cc.setName(name);
        cc.setExpensesBudget(BigDecimal.ZERO);
        cc.setRevenuesBudget(BigDecimal.ZERO);
        cc.setActive(active);
        return cc;
    }

    private MovementClass movementClass(String name, MovementClassType type, CostCenter costCenter) {
        var mc = new MovementClass();
        mc.setName(name);
        mc.setMovementClassType(type);
        mc.setCostCenter(costCenter);
        mc.setBudget(BigDecimal.ZERO);
        return mc;
    }
}
