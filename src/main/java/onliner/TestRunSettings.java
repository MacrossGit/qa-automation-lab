package onliner;

/**
 * Настройки запуска UI-тестов, не зависящие от Selenium.
 * Класс легко проверяется модульными тестами без открытия браузера.
 */
public final class TestRunSettings {
    private final boolean headless;

    public TestRunSettings(String headlessValue) {
        this.headless = Boolean.parseBoolean(headlessValue);
    }

    public static TestRunSettings fromSystemProperties() {
        return new TestRunSettings(System.getProperty("headless", "false"));
    }

    public boolean isHeadless() {
        return headless;
    }
}
