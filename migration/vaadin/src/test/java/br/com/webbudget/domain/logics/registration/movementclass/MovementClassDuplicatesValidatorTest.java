package br.com.webbudget.domain.logics.registration.movementclass;

import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.domain.entities.registration.MovementClass;
import br.com.webbudget.domain.entities.registration.MovementClassType;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.domain.repositories.registration.MovementClassRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MovementClassDuplicatesValidatorTest {

    @Test
    void throws_when_new_class_has_duplicated_name() {
        var costCenter = costCenter("Household");
        var existing = movementClass("Groceries", MovementClassType.EXPENSE, costCenter);
        var repo = mock(MovementClassRepository.class);
        when(repo.findByNameAndCostCenter_name("Groceries", "Household")).thenReturn(Optional.of(existing));

        var validator = new MovementClassDuplicatesValidator(repo);
        var newClass = movementClass("Groceries", MovementClassType.EXPENSE, costCenter);

        assertThatThrownBy(() -> validator.run(newClass))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessage("error.movement-class.duplicated");
    }

    @Test
    void passes_when_new_class_name_is_unique() {
        var costCenter = costCenter("Household");
        var repo = mock(MovementClassRepository.class);
        when(repo.findByNameAndCostCenter_name(any(), any())).thenReturn(Optional.empty());

        var validator = new MovementClassDuplicatesValidator(repo);
        var newClass = movementClass("Utilities", MovementClassType.EXPENSE, costCenter);

        assertThatCode(() -> validator.run(newClass)).doesNotThrowAnyException();
    }

    @Test
    void throws_when_saved_class_finds_same_name_different_instance() {
        var costCenter = costCenter("Household");
        var existing = movementClass("Groceries", MovementClassType.EXPENSE, costCenter);
        setId(existing, 99L);

        var repo = mock(MovementClassRepository.class);
        when(repo.findByNameAndCostCenter_name("Groceries", "Household")).thenReturn(Optional.of(existing));

        var validator = new MovementClassDuplicatesValidator(repo);
        var saved = movementClass("Groceries", MovementClassType.EXPENSE, costCenter);
        setId(saved, 99L); // same id -> equals() returns true in validateSaved check

        assertThatThrownBy(() -> validator.run(saved))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessage("error.movement-class.duplicated");
    }

    @Test
    void passes_when_saved_class_name_not_found_in_db() {
        var costCenter = costCenter("Household");
        var repo = mock(MovementClassRepository.class);
        when(repo.findByNameAndCostCenter_name(any(), any())).thenReturn(Optional.empty());

        var validator = new MovementClassDuplicatesValidator(repo);
        var saved = movementClass("Utilities", MovementClassType.EXPENSE, costCenter);
        setId(saved, 10L);

        assertThatCode(() -> validator.run(saved)).doesNotThrowAnyException();
    }

    private CostCenter costCenter(String name) {
        var cc = new CostCenter();
        cc.setName(name);
        cc.setExpensesBudget(BigDecimal.ZERO);
        cc.setRevenuesBudget(BigDecimal.ZERO);
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

    private void setId(MovementClass mc, long id) {
        try {
            var field = mc.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(mc, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
