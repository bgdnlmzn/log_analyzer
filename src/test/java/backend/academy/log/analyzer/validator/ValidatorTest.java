package backend.academy.log.analyzer.validator;

import backend.academy.log.analyzer.arguments.CliArguments;
import backend.academy.log.analyzer.arguments.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@DisplayName("Тесты Validator")
public class ValidatorTest {

    private Validator validator;

    private CliArguments cliArgs;

    @BeforeEach
    public void setUp() {
        validator = new Validator();
        cliArgs = spy(CliArguments.class);
    }

    @Test
    @DisplayName("Должен вернуть false, если путь равен null")
    public void shouldReturnFalseWhenPathIsNull() {
        when(cliArgs.path()).thenReturn(null);

        boolean isValid = validator.validate(cliArgs);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Должен вернуть false, если путь пустой")
    public void shouldReturnFalseWhenPathIsEmpty() {
        when(cliArgs.path()).thenReturn("");

        boolean isValid = validator.validate(cliArgs);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Должен вернуть true, если путь валидный")
    public void shouldReturnTrueWhenPathIsValid() {
        when(cliArgs.path()).thenReturn("/valid/path");

        boolean isValid = validator.validate(cliArgs);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Должен вернуть true, если дата равна null")
    public void shouldReturnTrueWhenDateIsNull() {
        when(cliArgs.path()).thenReturn("/valid/path");
        when(cliArgs.from()).thenReturn(null);

        boolean isValid = validator.validate(cliArgs);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Должен вернуть false, если дата невалидна")
    public void shouldReturnFalseWhenDateIsInvalid() {
        when(cliArgs.path()).thenReturn("/valid/path");
        when(cliArgs.from()).thenReturn("invalid-date");

        boolean isValid = validator.validate(cliArgs);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Должен вернуть true, если дата валидная")
    public void shouldReturnTrueWhenDateIsValid() {
        when(cliArgs.path()).thenReturn("/valid/path");
        when(cliArgs.from()).thenReturn("2024-11-17");

        boolean isValid = validator.validate(cliArgs);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Должен вернуть false, если формат пустой")
    public void shouldReturnFalseWhenFormatIsEmpty() {
        when(cliArgs.path()).thenReturn("/valid/path");
        when(cliArgs.format()).thenReturn("");

        boolean isValid = validator.validate(cliArgs);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Должен вернуть false, если формат невалиден")
    public void shouldReturnFalseWhenFormatIsInvalid() {
        when(cliArgs.path()).thenReturn("/valid/path");
        when(cliArgs.format()).thenReturn("xml");

        boolean isValid = validator.validate(cliArgs);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Должен вернуть true, если формат валидный")
    public void shouldReturnTrueWhenFormatIsValid() {
        when(cliArgs.path()).thenReturn("/valid/path");
        when(cliArgs.format()).thenReturn("markdown");

        boolean isValid = validator.validate(cliArgs);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Должен вернуть true, если поле фильтра и значение пустые")
    public void shouldReturnTrueWhenFilterFieldAndValueAreEmpty() {
        when(cliArgs.path()).thenReturn("/valid/path");
        when(cliArgs.filterField()).thenReturn(null);
        when(cliArgs.filterValue()).thenReturn(null);

        boolean isValid = validator.validate(cliArgs);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Должен вернуть false, если задано только поле фильтра")
    public void shouldReturnFalseWhenOnlyFilterFieldIsProvided() {
        when(cliArgs.path()).thenReturn("/valid/path");
        when(cliArgs.filterField()).thenReturn("remote_addr");
        when(cliArgs.filterValue()).thenReturn(null);

        boolean isValid = validator.validate(cliArgs);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Должен вернуть false, если поле фильтра невалидно")
    public void shouldReturnFalseWhenFilterFieldIsInvalid() {
        when(cliArgs.path()).thenReturn("/valid/path");
        when(cliArgs.filterField()).thenReturn("invalid_field");
        when(cliArgs.filterValue()).thenReturn("value");

        boolean isValid = validator.validate(cliArgs);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Должен вернуть true, если поле фильтра и значение валидные")
    public void shouldReturnTrueWhenFilterFieldAndValueAreValid() {
        when(cliArgs.path()).thenReturn("/valid/path");
        when(cliArgs.filterField()).thenReturn("remote_addr");
        when(cliArgs.filterValue()).thenReturn("value");

        boolean isValid = validator.validate(cliArgs);

        assertThat(isValid).isTrue();
    }
}
