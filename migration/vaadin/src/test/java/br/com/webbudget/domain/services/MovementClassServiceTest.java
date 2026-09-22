package br.com.webbudget.domain.services;

import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.domain.entities.registration.MovementClass;
import br.com.webbudget.domain.entities.registration.MovementClassType;
import br.com.webbudget.domain.logics.registration.movementclass.MovementClassSavingLogic;
import br.com.webbudget.domain.logics.registration.movementclass.MovementClassUpdatingLogic;
import br.com.webbudget.domain.repositories.registration.MovementClassRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MovementClassServiceTest {

    @Test
    void save_runs_saving_logics_and_persists() {
        var mc = movementClass("Groceries", MovementClassType.EXPENSE);
        var repo = mock(MovementClassRepository.class);
        var savingLogic = mock(MovementClassSavingLogic.class);

        var service = new MovementClassService(repo, List.of(savingLogic), List.of());
        service.save(mc);

        verify(savingLogic).run(mc);
        verify(repo).save(mc);
    }

    @Test
    void update_runs_updating_logics_and_refreshes() {
        var mc = movementClass("Groceries", MovementClassType.EXPENSE);
        var repo = mock(MovementClassRepository.class);
        when(repo.saveAndFlushAndRefresh(mc)).thenReturn(mc);
        var updatingLogic = mock(MovementClassUpdatingLogic.class);

        var service = new MovementClassService(repo, List.of(), List.of(updatingLogic));
        var result = service.update(mc);

        verify(updatingLogic).run(mc);
        verify(repo).saveAndFlushAndRefresh(mc);
        assertThat(result).isSameAs(mc);
    }

    @Test
    void delete_removes_movement_class() {
        var mc = movementClass("Groceries", MovementClassType.EXPENSE);
        var repo = mock(MovementClassRepository.class);

        var service = new MovementClassService(repo, List.of(), List.of());
        service.delete(mc);

        verify(repo).attachAndRemove(mc);
    }

    @Test
    void save_with_no_logics_still_persists() {
        var mc = movementClass("Salary", MovementClassType.REVENUE);
        var repo = mock(MovementClassRepository.class);

        var service = new MovementClassService(repo, List.of(), List.of());
        service.save(mc);

        verify(repo).save(mc);
    }

    private MovementClass movementClass(String name, MovementClassType type) {
        var costCenter = new CostCenter();
        costCenter.setName("General");
        costCenter.setExpensesBudget(BigDecimal.ZERO);
        costCenter.setRevenuesBudget(BigDecimal.ZERO);

        var mc = new MovementClass();
        mc.setName(name);
        mc.setMovementClassType(type);
        mc.setCostCenter(costCenter);
        mc.setBudget(BigDecimal.ZERO);
        return mc;
    }
}
