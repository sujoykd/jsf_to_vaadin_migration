package br.com.webbudget.application.components.ui.filter;

import br.com.webbudget.domain.entities.financial.FixedMovementState;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Optional;

@ToString
@EqualsAndHashCode
public final class FixedMovementFilter {

    @Setter
    @Getter
    private String value;

    @Setter
    @Getter
    private FixedMovementState fixedMovementState;

    public FixedMovementFilter() {
        this.clear();
    }

    public void clear() {
        this.value = null;
        this.fixedMovementState = FixedMovementState.ACTIVE;
    }

    public Optional<BigDecimal> valueToBigDecimal() {
        try {
            return Optional.of(new BigDecimal(this.value));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public FixedMovementState[] getFixedMovementStates() {
        return FixedMovementState.values();
    }
}
