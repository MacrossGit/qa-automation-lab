package onliner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Модульные тесты правила запуска авторизационного сценария. */
class TestAccountTest {

    @Test
    void accountIsConfiguredWhenEmailAndPasswordArePresent() {
        TestAccount account = new TestAccount("qa@example.com", "safe-password");

        assertTrue(account.isConfigured());
    }

    @Test
    void accountIsNotConfiguredWhenAnyValueIsBlank() {
        assertFalse(new TestAccount("", "password").isConfigured());
        assertFalse(new TestAccount("qa@example.com", " ").isConfigured());
    }
}
