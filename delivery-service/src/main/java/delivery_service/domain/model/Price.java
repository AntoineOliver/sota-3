package delivery_service.domain.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public final class Price {

    private final BigDecimal amount;
    private final Currency currency;

    public Price(double amount, String currencyCode) {
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
        if (!(o instanceof Price price)) return false;
        return amount.equals(price.amount) && currency.equals(price.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}
