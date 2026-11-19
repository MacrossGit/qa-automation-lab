package saucedemo;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

/**
 * Небольшой Page Object: здесь живут локаторы и повторяющиеся действия,
 * а сами тесты остаются короткими и читаются как пользовательские сценарии.
 */
class SauceDemoPage {
    /** Адрес тестового Shopify-магазина. */
    private static final String BASE_URL = "https://sauce-demo.myshopify.com";

    /** Адрес общего каталога товаров. */
    private static final String CATALOG_URL = BASE_URL + "/collections/all";

    /** Учебная задержка, которую пользователь видит после каждого действия. */
    private static final Duration DEMO_DELAY = Duration.ofSeconds(1);

    /** Увеличенная задержка после очистки корзины для визуальной проверки результата. */
    private static final Duration CART_CLEARED_DELAY = Duration.ofSeconds(10);

    /** Selenium-драйвер текущего браузера. */
    private final WebDriver driver;

    /** Явное ожидание появления и кликабельности элементов. */
    private final WebDriverWait wait;

    /**
     * Создаёт объект страницы для текущего браузера.
     *
     * @param driver браузер, открытый базовым тестом
     * @param wait ожидание Selenium
     */
    SauceDemoPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    /** Открывает главную страницу магазина. */
    void openHomePage() {
        navigateTo(BASE_URL, "Открываю главную страницу магазина");
    }

    /** Открывает общий каталог, в котором представлены все товары. */
    void openCatalog() {
        navigateTo(CATALOG_URL, "Открываю каталог товаров");
    }

    /** Открывает форму входа покупателя. */
    void openLoginPage() {
        navigateTo(BASE_URL + "/account/login", "Открываю форму авторизации");
    }

    /** Открывает форму регистрации нового покупателя. */
    void openRegistrationPage() {
        navigateTo(BASE_URL + "/account/register", "Открываю форму регистрации");
    }

    /**
     * Заполняет и отправляет форму регистрации случайными данными тестового покупателя.
     * Пароль намеренно не выводится в консоль.
     *
     * Если сайт запросит CAPTCHA, метод не пытается обходить её: сообщает об ограничении
     * и позволяет E2E-сценарию продолжиться как неавторизованному покупателю.
     *
     * @param customer данные нового аккаунта, созданные в текущем прогоне
     * @return {@code true}, если аккаунт создан и пользователь вошёл; {@code false}, если показана CAPTCHA
     */
    boolean registerNewCustomer(TestCustomer customer) {
        log("Регистрирую новый аккаунт: " + customer.email());
        type(By.cssSelector("input[name='customer[first_name]'], input#FirstName"),
                customer.firstName(), "Ввожу имя");
        type(By.cssSelector("input[name='customer[last_name]'], input#LastName"),
                customer.lastName(), "Ввожу фамилию");
        type(By.cssSelector("input[name='customer[email]'], input#Email"),
                customer.email(), "Ввожу случайный e-mail");
        type(By.cssSelector("input[name='customer[password]'], input#CreatePassword"),
                customer.password(), "Ввожу пароль нового аккаунта");
        click(By.cssSelector("form[action*='/account'] input[type='submit'], form[action*='/account'] button[type='submit']"),
                "Подтверждаю регистрацию");
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                    ignored -> isAuthenticated() || !registrationErrorText().isBlank() || isCaptchaDisplayed());
        } catch (TimeoutException exception) {
            log("Регистрация не завершилась за время ожидания — продолжаю демо без авторизации");
            return false;
        }

        if (isCaptchaDisplayed()) {
            log("Показана CAPTCHA — не обхожу её и продолжаю сценарий без авторизации");
            pause();
            return false;
        }

        if (!isAuthenticated()) {
            throw new IllegalStateException("Shopify отклонил регистрацию: " + registrationErrorText());
        }
        return true;
    }

    /**
     * Проверяет, что пользователь вошёл в свой кабинет после регистрации.
     *
     * @return {@code true}, если страница аккаунта открыта или видна ссылка выхода
     */
    boolean isAuthenticated() {
        return driver.getCurrentUrl().equals(BASE_URL + "/account")
                || hasVisibleElement(By.xpath("//a[contains(translate(normalize-space(), "
                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'log out')]"));
    }

    /**
     * Заполняет форму входа и отправляет её.
     * На этом Shopify-сайте поле логина — e-mail, поэтому логин Swag Labs
     * {@code standard_user} сознательно приводит к сообщению об ошибке.
     *
     * @param login значение для поля e-mail
     * @param password пароль
     */
    void submitLogin(String login, String password) {
        type(By.id("customer_email"), login, "Ввожу логин");
        type(By.id("customer_password"), password, "Ввожу пароль");
        click(By.cssSelector("input[type='submit'], button[type='submit']"), "Отправляю форму авторизации");
    }

    /**
     * Возвращает текст области с ошибкой авторизации.
     *
     * @return сообщение об ошибке или пустая строка, если оно не показано
     */
    String loginErrorText() {
        List<WebElement> errors = driver.findElements(By.cssSelector(".errors, .error, [role='alert']"));
        return errors.stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .findFirst()
                .orElse("");
    }

    /**
     * Возвращает число ссылок на товары в открытом каталоге.
     * Локатор ограничен ссылками на product, чтобы не посчитать навигацию.
     */
    int catalogProductCount() {
        return driver.findElements(By.cssSelector("a[href*='/products/']")).size();
    }

    /**
     * Выполняет один шаг прокрутки вниз, затем возвращается одним шагом наверх.
     * Такой вариант наглядно демонстрирует оба направления и не уводит сценарий в конец страницы.
     */
    void scrollDownAndBackUp() {
        log("Прокручиваю страницу на один шаг вниз");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(0, Math.floor(window.innerHeight * 0.8));");
        pause();

        log("Возвращаюсь на один шаг вверх");
        js.executeScript("window.scrollBy(0, -Math.floor(window.innerHeight * 0.8));");
        pause();
    }

    /**
     * Открывает карточку товара по его читаемому названию.
     *
     * @param productName точное имя товара, например {@code Grey jacket}
     */
    void openProduct(String productName) {
        openCatalog();
        click(By.xpath("//a[contains(@href, '/products/') and contains(normalize-space(), "
                        + xpathLiteral(productName) + ")]"),
                "Открываю товар: " + productName);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
    }

    /**
     * Меняет значение каждого выпадающего списка варианта, если в нём есть доступная альтернатива.
     * Товары без select не считаются ошибкой: у них единственный вариант, который уже выбран сайтом.
     *
     * @return {@code true}, если хотя бы один вариант товара был реально изменён
     */
    boolean selectAlternativeVariantsIfAvailable() {
        List<WebElement> selects = driver.findElements(By.cssSelector("form[action*='/cart/add'] select"))
                .stream()
                .filter(WebElement::isDisplayed)
                .toList();
        boolean variantChanged = false;

        if (selects.isEmpty()) {
            log("У товара нет выпадающего списка — оставляю единственный вариант");
            pause();
            return false;
        }

        for (WebElement selectElement : selects) {
            Select select = new Select(selectElement);
            List<WebElement> options = select.getOptions();
            int selectedIndex = options.indexOf(select.getFirstSelectedOption());
            int alternativeIndex = findAlternativeOptionIndex(options, selectedIndex);

            if (alternativeIndex >= 0) {
                log("Выбираю другой вариант: " + options.get(alternativeIndex).getText());
                select.selectByIndex(alternativeIndex);
                variantChanged = true;
                pause();
            }
        }
        return variantChanged;
    }

    /**
     * Добавляет открытый товар в корзину через кнопку на карточке.
     *
     * @param productName название товара только для сообщения в консоль
     */
    void addOpenedProductToCart(String productName) {
        click(By.cssSelector("form[action*='/cart/add'] input[type='submit'], form[action*='/cart/add'] button[type='submit']"),
                "Добавляю в корзину: " + productName);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/cart"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href*='/cart']"))));
    }

    /**
     * Добавляет товар в корзину, только когда кнопка добавления доступна.
     * Это позволяет одному E2E-сценарию пройти все карточки, включая распроданные позиции.
     *
     * @param productName название товара для консольного сообщения
     * @return {@code true}, если товар был добавлен; {@code false}, если он недоступен
     */
    boolean addOpenedProductToCartIfAvailable(String productName) {
        By addToCartButton = By.cssSelector(
                "form[action*='/cart/add'] input[type='submit'], form[action*='/cart/add'] button[type='submit']");
        WebElement button = firstVisibleElement(addToCartButton);

        if (button == null || !button.isEnabled()) {
            log("Товар недоступен — пропускаю: " + productName);
            pause();
            return false;
        }

        addOpenedProductToCart(productName);
        return true;
    }

    /** Открывает страницу корзины. */
    void openCart() {
        navigateTo(BASE_URL + "/cart", "Открываю корзину");
    }

    /**
     * Проверяет, что заданное название видно в корзине.
     *
     * @param productName ожидаемое имя товара
     * @return {@code true}, если товар присутствует в тексте страницы
     */
    boolean cartContains(String productName) {
        return driver.findElement(By.tagName("body")).getText().contains(productName);
    }

    /**
     * Удаляет все товары из корзины кликами по кнопке-крестику каждой позиции.
     * В отличие от перехода на технический {@code /cart/clear.js}, этот путь
     * повторяет реальное действие покупателя и подходит для учебного UI-теста.
     */
    void clearCart() {
        openCart();
        // В текущей теме Shopify удаление оформлено как крестик со ссылкой /cart/change?quantity=0.
        // Запасные условия оставлены на случай, если тема заменит крестик кнопкой с aria-label.
        By removeCross = By.xpath("//a[contains(@href, '/cart/change') or normalize-space()='×']"
                + " | //button[contains(@aria-label, 'Remove') or normalize-space()='×']");

        while (hasVisibleElement(removeCross)) {
            int crossesBeforeClick = driver.findElements(removeCross).size();
            click(removeCross, "Нажимаю крестик у очередного товара");
            wait.until(currentDriver -> currentDriver.findElements(removeCross).size() < crossesBeforeClick);
        }
        log("Корзина очищена");
        log("Оставляю пустую корзину открытой на 10 секунд");
        pause(CART_CLEARED_DELAY);
    }

    /**
     * Возвращает признак пустой корзины по отсутствию ссылок Remove.
     *
     * @return {@code true}, когда в корзине нет позиций
     */
    boolean isCartEmpty() {
        return !hasVisibleElement(By.xpath("//a[contains(@href, '/cart/change') or normalize-space()='×']"
                + " | //button[contains(@aria-label, 'Remove') or normalize-space()='×']"));
    }

    /** Переходит по адресу, ожидает загрузку документа и делает демонстрационную паузу. */
    private void navigateTo(String url, String message) {
        log(message);
        driver.get(url);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        pause();
    }

    /** Вводит значение в поле, предварительно очищая его. */
    private void type(By locator, String value, String message) {
        log(message);
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        field.clear();
        field.sendKeys(value);
        pause();
    }

    /** Кликает по первому видимому элементу, подходящему под локатор. */
    private void click(By locator, String message) {
        log(message);
        WebElement element = wait.until(driver -> firstVisibleElement(locator));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", element);
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        pause();
    }

    /** Находит первый видимый элемент или возвращает {@code null}. */
    private WebElement firstVisibleElement(By locator) {
        return driver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null);
    }

    /** Проверяет наличие хотя бы одного видимого элемента. */
    private boolean hasVisibleElement(By locator) {
        return firstVisibleElement(locator) != null;
    }

    /** Возвращает сообщение об ошибке из формы регистрации или пустую строку, если ошибки нет. */
    private String registrationErrorText() {
        return driver.findElements(By.cssSelector(".errors, .error, [role='alert']")).stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .filter(text -> !text.isBlank())
                .findFirst()
                .orElse("");
    }

    /**
     * Определяет видимую CAPTCHA по стандартным iframe, атрибутам и тексту страницы.
     * Метод только распознаёт проверку; он не решает CAPTCHA и не пытается её обходить.
     */
    private boolean isCaptchaDisplayed() {
        boolean hasCaptchaWidget = driver.findElements(By.cssSelector(
                        "iframe[src*='captcha'], iframe[title*='captcha'], [data-sitekey], .g-recaptcha, .h-captcha"))
                .stream()
                .anyMatch(WebElement::isDisplayed);
        String bodyText = driver.findElement(By.tagName("body")).getText().toLowerCase(Locale.ROOT);
        return hasCaptchaWidget || bodyText.contains("captcha") || bodyText.contains("verify you are human");
    }

    /** Находит индекс первой доступной опции, которая отличается от текущей. */
    private int findAlternativeOptionIndex(List<WebElement> options, int selectedIndex) {
        for (int index = 0; index < options.size(); index++) {
            WebElement option = options.get(index);
            if (index != selectedIndex && option.isEnabled() && !option.getAttribute("value").isBlank()) {
                return index;
            }
        }
        return -1;
    }

    /** Выводит короткий статус текущего шага в консоль. */
    private void log(String message) {
        System.out.println("[UI] " + message);
    }

    /** Делает обязательную двухсекундную паузу для наблюдения за браузером. */
    private void pause() {
        pause(DEMO_DELAY);
    }

    /**
     * Приостанавливает тест на указанное время, не закрывая браузер.
     *
     * @param duration продолжительность визуальной паузы
     */
    private void pause(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Демонстрационный тест был прерван", exception);
        }
    }

    /** Превращает Java-строку в безопасный текстовый литерал XPath. */
    private String xpathLiteral(String text) {
        if (!text.contains("'")) {
            return "'" + text + "'";
        }
        return "\"" + text + "\"";
    }
}
