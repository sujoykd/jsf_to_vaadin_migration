package br.com.webbudget.vaadin.views.registration;

import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.entities.registration.MovementClassType;
import br.com.webbudget.domain.entities.view.PeriodResult;
import br.com.webbudget.domain.entities.view.UseByCostCenter;
import br.com.webbudget.domain.entities.view.UseByMovementClass;
import br.com.webbudget.domain.repositories.registration.FinancialPeriodRepository;
import br.com.webbudget.domain.repositories.view.PeriodResultRepository;
import br.com.webbudget.domain.repositories.view.UseByCostCenterRepository;
import br.com.webbudget.domain.repositories.view.UseByMovementClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class FinancialPeriodStatisticsPresenter {

    private final FinancialPeriodRepository financialPeriodRepository;
    private final PeriodResultRepository periodResultRepository;
    private final UseByCostCenterRepository useByCostCenterRepository;
    private final UseByMovementClassRepository useByMovementClassRepository;

    public Optional<FinancialPeriod> findById(long id) {
        return financialPeriodRepository.findById(id);
    }

    public Optional<PeriodResult> findPeriodResult(long financialPeriodId) {
        return periodResultRepository.findByFinancialPeriodId(financialPeriodId);
    }

    public List<UseByCostCenter> findExpensesByCostCenter(long financialPeriodId) {
        return useByCostCenterRepository.findByFinancialPeriodIdAndDirection(financialPeriodId, MovementClassType.EXPENSE);
    }

    public List<UseByCostCenter> findRevenuesByCostCenter(long financialPeriodId) {
        return useByCostCenterRepository.findByFinancialPeriodIdAndDirection(financialPeriodId, MovementClassType.REVENUE);
    }

    public List<UseByMovementClass> findExpensesByMovementClass(long financialPeriodId) {
        return useByMovementClassRepository.findByFinancialPeriodIdAndDirection(financialPeriodId, MovementClassType.EXPENSE);
    }

    public List<UseByMovementClass> findRevenuesByMovementClass(long financialPeriodId) {
        return useByMovementClassRepository.findByFinancialPeriodIdAndDirection(financialPeriodId, MovementClassType.REVENUE);
    }
}
