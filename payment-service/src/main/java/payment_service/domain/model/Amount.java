package payment_service.domain.model;

import common.ddd.ValueObject;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public final class Amount implements ValueObject {

    private final BigDecimal amount;
    private final Currency currency;

    public Amount(double amount, String currencyCode) {
        if (amount < 0) {
            throw new IllegalArgumentException("Price amount must be non-negative");
        }
        this.amount = BigDecimal.valueOf(amount);
        this.currency = Currency.getInstance(currencyCode);
    }

    public BigDecimal amount() {
        return amount;
    }

    public Currency currency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Amount price)) return false;
        return amount.equals(price.amount) && currency.equals(price.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}
