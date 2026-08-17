package br.com.matheus.commerceapi.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Email Tests")
class EmailTest {

    @Test
    @DisplayName("Should accept a valid email")
    void shouldAcceptValidEmail() {
        assertThat(Email.of("user@email.com").value()).isEqualTo("user@email.com");
    }

    @Test
    @DisplayName("Should trim surrounding whitespace")
    void shouldTrimWhitespace() {
        assertThat(Email.of("  user@email.com  ").value()).isEqualTo("user@email.com");
    }

    @Test
    @DisplayName("Should normalize to lowercase")
    void shouldNormalizeToLowerCase() {
        assertThat(Email.of("User@Email.COM").value()).isEqualTo("user@email.com");
    }

    @Test
    @DisplayName("Should reject null email")
    void shouldRejectNullEmail() {
        assertThatThrownBy(() -> Email.of(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should reject blank email")
    void shouldRejectBlankEmail() {
        assertThatThrownBy(() -> Email.of("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email format");
    }

    @Test
    @DisplayName("Should reject email without valid format")
    void shouldRejectInvalidFormat() {
        assertThatThrownBy(() -> Email.of("invalid_email"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email format");
    }

    @Test
    @DisplayName("Should reject email longer than 100 characters")
    void shouldRejectTooLongEmail() {
        String longEmail = "user@"
                + "a".repeat(63) + "."
                + "b".repeat(63) + "."
                + "c".repeat(30) + ".com";

        assertThatThrownBy(() -> Email.of(longEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100 characters");
    }

    @Test
    @DisplayName("Should be equal regardless of case and whitespace")
    void shouldBeEqualAfterNormalization() {
        assertThat(Email.of("  User@Email.COM  ")).isEqualTo(Email.of("user@email.com"));
        assertThat(Email.of("  User@Email.COM  ").hashCode())
                .isEqualTo(Email.of("user@email.com").hashCode());
    }

    @Test
    @DisplayName("Should print value as plain string")
    void shouldPrintValue() {
        assertThat(Email.of("user@email.com").toString()).isEqualTo("user@email.com");
    }
}