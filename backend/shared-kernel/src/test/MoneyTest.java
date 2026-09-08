package com.logistics.shared.domain.valueobject;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Currency;
import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldAddTwoMoniesWithSameCurrency() {
        Money m1 = Money.usd(BigDecimal.valueOf(10.50));
        Money m2 = Money.usd(BigDecimal.valueOf(5.25));

        Money result = m1.add(m2);

        assertThat(result.amount()).isEqualByComparingTo("15.75");
        assertThat(result.currency().getCurrencyCode()).isEqualTo("USD");
    }

    @Test
    void shouldThrowWhenAddingDifferentCurrencies() {
        Money usd = Money.usd(BigDecimal.TEN);
        Money eur = new Money(BigDecimal.TEN, Currency.getInstance("EUR"));

        assertThatThrownBy(() -> usd.add(eur))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("different currencies");
    }
}