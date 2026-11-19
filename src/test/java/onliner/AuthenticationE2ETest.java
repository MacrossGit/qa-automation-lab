package onliner;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Проверка входа. Секреты передаются только через переменные окружения. */
class AuthenticationE2ETest extends BaseUiTest {

    @Test
    void registeredUserCanSignIn() {
        TestAccount account = TestAccount.fromEnvironment();

        // Это позволяет безопасно запускать остальные тесты на CI без секрета.
        Assumptions.assumeTrue(account.isConfigured(),
                "Задайте ONLINER_EMAIL и ONLINER_PASSWORD для проверки входа");

        OnlinerPage page = new OnlinerPage(driver, wait);
        page.openHomePage();
        page.openLogin();
        page.login(account.email(), account.password());

        // После успешного входа кнопка «Вход» пропадает из шапки.
        assertTrue(page.isAuthenticated(),
                "После успешного входа не должна отображаться кнопка входа");
    }
}
