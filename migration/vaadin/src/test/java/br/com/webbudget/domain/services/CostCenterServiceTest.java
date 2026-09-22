package br.com.webbudget.domain.services;

import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.domain.logics.registration.costcenter.CostCenterSavingLogic;
import br.com.webbudget.domain.logics.registration.costcenter.CostCenterUpdatingLogic;
import br.com.webbudget.domain.repositories.registration.CostCenterRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CostCenterServiceTest {

    @Test
    void save_runs_saving_logics_and_persists() {
        var cc = costCenter("Household");
        var repo = mock(CostCenterRepository.class);
        var savingLogic = mock(CostCenterSavingLogic.class);

        var service = new CostCenterService(repo, List.of(savingLogic), List.of());
        service.save(cc);

        verify(savingLogic).run(cc);
        verify(repo).save(cc);
    }

    @Test
    void update_runs_updating_logics_and_refreshes() {
        var cc = costCenter("Household");
        var repo = mock(CostCenterRepository.class);
        when(repo.saveAndFlushAndRefresh(cc)).thenReturn(cc);
        var updatingLogic = mock(CostCenterUpdatingLogic.class);

        var service = new CostCenterService(repo, List.of(), List.of(updatingLogic));
        var result = service.update(cc);

        verify(updatingLogic).run(cc);
        verify(repo).saveAndFlushAndRefresh(cc);
        assertThat(result).isSameAs(cc);
    }

    @Test
    void delete_removes_cost_center() {
        var cc = costCenter("Household");
        var repo = mock(CostCenterRepository.class);

        var service = new CostCenterService(repo, List.of(), List.of());
        service.delete(cc);

        verify(repo).attachAndRemove(cc);
    }

    @Test
    void save_with_no_logics_still_persists() {
        var cc = costCenter("Transport");
        var repo = mock(CostCenterRepository.class);

        var service = new CostCenterService(repo, List.of(), List.of());
        service.save(cc);

        verify(repo).save(cc);
    }

    private CostCenter costCenter(String name) {
        var cc = new CostCenter();
        cc.setName(name);
        cc.setExpensesBudget(BigDecimal.ZERO);
        cc.setRevenuesBudget(BigDecimal.ZERO);
        return cc;
    }
}
