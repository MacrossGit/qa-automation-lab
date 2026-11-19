package saucedemo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Общая настройка браузера для учебных UI-тестов Sauce Demo.
 * Каждый тест получает новый Chrome и чистую корзину браузера.
 */
abstract class BaseUiTest {
    /** Браузер, которым управляет тест. */
    protected WebDriver driver;

    /** Явное ожидание для динамически появляющихся элементов. */
    protected WebDriverWait wait;

    /**
     * Запускает видимый Chrome перед каждым тестом.
     * Для запуска в CI без окна можно передать {@code -Dheadless=true}.
     */
    @BeforeEach
    void openBrowser() {
        System.out.println("[SETUP] Открываю браузер Chrome");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1440,1000");
        options.addArguments("--disable-notifications");
        // Инкогнито не сохраняет cookies и данные корзины после закрытия окна.
        options.addArguments("--incognito");

        // Альтернатива для CI: раскомментировать следующую строку.
        // options.addArguments("--headless=new");
        if (Boolean.parseBoolean(System.getProperty("headless", "false"))) {
            options.addArguments("--headless=new");
        }

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Закрывает браузер после теста, даже если проверка завершилась ошибкой.
     */
    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            System.out.println("[TEARDOWN] Закрываю браузер");
            driver.quit();
        }
    }
}
