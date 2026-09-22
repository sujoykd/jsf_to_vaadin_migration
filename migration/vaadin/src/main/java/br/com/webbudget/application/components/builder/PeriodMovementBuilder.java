package br.com.webbudget.application.components.builder;

import br.com.webbudget.domain.entities.financial.Apportionment;
import br.com.webbudget.domain.entities.financial.PeriodMovement;
import br.com.webbudget.domain.entities.financial.PeriodMovementType;
import br.com.webbudget.domain.entities.registration.Contact;
import br.com.webbudget.domain.entities.registration.FinancialPeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public final class PeriodMovementBuilder extends AbstractBuilder<PeriodMovement> {

    public PeriodMovementBuilder() {
        this.instance = new PeriodMovement();
    }

    public PeriodMovementBuilder identification(String identification) {
        this.instance.setIdentification(identification);
        return this;
    }

    public PeriodMovementBuilder description(String description) {
        this.instance.setDescription(description);
        return this;
    }

    public PeriodMovementBuilder dueDate(LocalDate dueDate) {
        this.instance.setDueDate(dueDate);
        return this;
    }

    public PeriodMovementBuilder type(PeriodMovementType periodMovementType) {
        this.instance.setPeriodMovementType(periodMovementType);
        return this;
    }

    public PeriodMovementBuilder financialPeriod(FinancialPeriod financialPeriod) {
        this.instance.setFinancialPeriod(financialPeriod);
        return this;
    }

    public PeriodMovementBuilder value(BigDecimal value) {
        this.instance.setValue(value);
        return this;
    }

    public PeriodMovementBuilder contact(Contact contact) {
        this.instance.setContact(contact);
        return this;
    }

    public PeriodMovementBuilder addApportionment(Apportionment apportionment) {
        this.instance.add(apportionment);
        return this;
    }

    public PeriodMovementBuilder addApportionments(Set<Apportionment> apportionments) {
        this.instance.addAll(apportionments);
        return this;
    }

    @Override
    public PeriodMovement build() {
        return this.instance;
    }
}
