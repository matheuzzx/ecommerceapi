package br.com.matheus.commerceapi.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money Tests")
class MoneyTest {

    @Test
    @DisplayName("Should normalize amount to two decimal places with HALF_UP rounding")
    void shouldNormalizeAmountToTwoDecimals() {
        Money money = Money.of(new BigDecimal("10.5"));

        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("10.50"));
        assertThat(money.amount().scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should round half up when amount has more than two decimals")
    void shouldRoundHalfUp() {
        assertThat(Money.of(new BigDecimal("10.005")).amount())
                .isEqualByComparingTo(new BigDecimal("10.01"));
        assertThat(Money.of(new BigDecimal("10.004")).amount())
                .isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("Should reject null amount")
    void shouldRejectNullAmount() {
        assertThatThrownBy(() -> Money.of(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should reject negative amount")
    void shouldRejectNegativeAmount() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("-1.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    @Test
    @DisplayName("Should allow zero amount")
    void shouldAllowZeroAmount() {
        assertThat(Money.zero().amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should add amounts")
    void shouldAddAmounts() {
        Money sum = Money.of(new BigDecimal("10.50")).add(Money.of(new BigDecimal("5.25")));

        assertThat(sum.amount()).isEqualByComparingTo(new BigDecimal("15.75"));
    }

    @Test
    @DisplayName("Should multiply amount by quantity")
    void shouldMultiplyAmount() {
        Money total = Money.of(new BigDecimal("10.50")).multiply(3);

        assertThat(total.amount()).isEqualByComparingTo(new BigDecimal("31.50"));
    }

    @Test
    @DisplayName("Should be equal regardless of scale")
    void shouldBeEqualRegardlessOfScale() {
        assertThat(Money.of(new BigDecimal("10"))).isEqualTo(Money.of(new BigDecimal("10.00")));
        assertThat(Money.of(new BigDecimal("10")).hashCode())
                .isEqualTo(Money.of(new BigDecimal("10.00")).hashCode());
    }

    @Test
    @DisplayName("Should print plain string without trailing zeros")
    void shouldPrintPlainString() {
        assertThat(Money.of(new BigDecimal("10.50")).toString()).isEqualTo("10.50");
    }
}
