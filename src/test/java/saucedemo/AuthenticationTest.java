package saucedemo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Отдельные проверки формы авторизации Shopify-магазина. */
class AuthenticationTest extends BaseUiTest {
    /**
     * Демонстрирует заполнение и отправку формы входа.
     * Учётные данные взяты из задания, но предназначены для другого сайта — Swag Labs.
     */
    @Test
    void swagLabsCredentialsAreRejectedByShopifyLogin() {
        SauceDemoPage page = new SauceDemoPage(driver, wait);

        page.openLoginPage();
        page.submitLogin("standard_user", "secret_sauce");

        assertFalse(page.loginErrorText().isBlank(),
                "Shopify должен показать ошибку, поскольку standard_user не является e-mail аккаунтом магазина");
    }

    /** Проверяет, что доступна сама форма авторизации с e-mail и паролем. */
    @Test
    void loginFormIsDisplayed() {
        SauceDemoPage page = new SauceDemoPage(driver, wait);

        page.openLoginPage();

        assertTrue(driver.findElement(org.openqa.selenium.By.id("customer_email")).isDisplayed(),
                "Поле e-mail должно быть видно пользователю");
        assertTrue(driver.findElement(org.openqa.selenium.By.id("customer_password")).isDisplayed(),
                "Поле пароля должно быть видно пользователю");
    }
}
