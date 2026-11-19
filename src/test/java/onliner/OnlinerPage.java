package onliner;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/**
 * Небольшой Page Object для повторяющихся действий в каталоге.
 * Локаторы собраны здесь, чтобы при изменении сайта их было легко обновить.
 */
class OnlinerPage {
    private static final String BASE_URL = "https://catalog.onliner.by/";
    private static final String PROFILE_LOGIN_URL = "https://profile.onliner.by/login";
    private static final List<String> MAIN_CATALOGS = List.of(
            "/cat/electronics",
            "/cat/computers",
            "/cat/appliance",
            "/cat/fmcg",
            "/cat/building",
            "/cat/household",
            "/cat/auto",
            "/cat/lifestyle",
            "/cat/kids"
    );
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final boolean demoMode;

    OnlinerPage(WebDriver driver, WebDriverWait wait) {
        this(driver, wait, false);
    }

    OnlinerPage(WebDriver driver, WebDriverWait wait, boolean demoMode) {
        this.driver = driver;
        this.wait = wait;
        this.demoMode = demoMode;
    }

    void openHomePage() {
        showAction("Открываю главную страницу каталога");
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
    }

    void searchFor(String query) {
        showAction("Ввожу в поиск: " + query);
        // Поле поиска в шапке имеет стабильный CSS-класс fast-search__input.
        WebElement search = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input.fast-search__input")));
        search.clear();
        search.sendKeys(query);
        // Поиск показывает выпадающие подсказки, не меняя адрес страницы.
        wait.until(ExpectedConditions.attributeToBe(search, "value", query));
        pause();
    }

    /**
     * Открывает все главные разделы каталога. Каждый раздел открывается кликом
     * и прокручивается вниз: это демонстрирует обычную навигацию пользователя.
     */
    void exploreMainCatalogs() {
        for (String catalogPath : MAIN_CATALOGS) {
            showAction("Открываю раздел: " + catalogPath);
            // Используем адреса тех же карточек главной страницы. Список фиксирован,
            // потому что промо-карточки Onliner меняются во время одного теста.
            driver.get(BASE_URL + catalogPath.substring(1));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
            scrollToBottom();
        }
    }

    /** Ищет товар через строку поиска и открывает первую подходящую карточку. */
    void searchAndOpenProduct(String productName) {
        openHomePage();
        searchFor(productName);

        // В поисковой выдаче Onliner переход в предложения выполняется оранжевой
        // ссылкой «N предложений», а не названием товара.
        By offersButton = By.xpath("//*[self::a or self::button][contains(normalize-space(), 'предложен')]");
        showAction("Нажимаю кнопку с предложениями");
        clickFirstVisible(offersButton);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        pause();
    }

    String pageText() {
        return driver.findElement(By.tagName("body")).getText();
    }

    /** Возвращает true, если в шапке больше нет кнопки «Вход». */
    boolean isAuthenticated() {
        return driver.findElements(By.cssSelector(".auth-bar__item--text"))
                .stream()
                .filter(WebElement::isDisplayed)
                .noneMatch(element -> element.getText().trim().equals("Вход"));
    }

    String searchValue() {
        return driver.findElement(By.cssSelector("input.fast-search__input")).getAttribute("value");
    }

    void openCitySelector() {
        // Ссылка города находится в блоке предложений продавцов на странице товара.
        clickFirstVisible(By.xpath("//a[contains(@class, 'h_txt_ref') and normalize-space()='Минск']"));
    }

    void chooseCity(String city) {
        // В окне выбора пункт содержит также тип населённого пункта, например «г. Брест».
        clickFirstVisible(By.xpath("//*[self::button or self::a or @role='option'][contains(normalize-space(), " + quote(city) + ")]"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    void openLogin() {
        showAction("Открываю форму авторизации");
        // Каталог открывает форму во всплывающем виджете. Для надёжного E2E-потока
        // используем ту же официальную страницу профиля напрямую.
        driver.get(PROFILE_LOGIN_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("a.auth-form__button_choose")));
        // По умолчанию форма предлагает вход по телефону. Переключаемся на e-mail.
        clickFirstVisible(By.xpath("//a[contains(@class, 'auth-form__button_choose') and contains(., 'e-mail')]"));
    }

    void login(String email, String password) {
        showAction("Ввожу email для авторизации");
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input.auth-input, input[type='email'], input[name='login'], input[name='email']")));
        emailField.sendKeys(email);
        clickFirstVisible(By.cssSelector("button[type='submit'].auth-button"));

        showAction("Ввожу пароль и подтверждаю вход");
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='password'], input[name='password']")));
        passwordField.sendKeys(password);
        clickFirstVisible(By.cssSelector("button[type='submit'].auth-button"));

        if (demoMode) {
            System.out.println("[DEMO] CAPTCHA показана. Возвращаюсь в каталог и продолжаю демонстрацию.");
        }
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        pause();
    }

    void openTestProduct() {
        showAction("Открываю тестовый товар");
        // Фиксированный массовый товар делает сценарий корзины воспроизводимым.
        driver.get("https://catalog.onliner.by/mobile/xiaomi/note17prom8512wt");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
    }

    void addFirstOfferToCart() {
        // В списке предложений действие называется «Купить» и сразу добавляет товар в корзину.
        showAction("Добавляю товар в корзину");
        clickFirstVisible(By.xpath("//a[contains(@class, 'but') and normalize-space()='Купить']"));
        pause();
    }

    void openCart() {
        // Прямой переход исключает повторный клик по кнопке «Купить» в карточке товара.
        showAction("Открываю корзину");
        driver.get("https://cart.onliner.by/");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
    }

    void removeFirstItemFromCart() {
        showAction("Удаляю товар из корзины");
        clickFirstVisible(By.xpath("//button[contains(@aria-label, 'Удал') or contains(normalize-space(), 'Удалить')]"));
    }

    /** Удаляет все позиции из тестовой корзины, не переходя к оформлению заказа. */
    void clearCart() {
        showAction("Очищаю тестовую корзину");
        openCart();
        By removeButton = By.xpath("//button[contains(@aria-label, 'Удал') or contains(normalize-space(), 'Удалить')]");

        while (driver.findElements(removeButton).stream().anyMatch(WebElement::isDisplayed)) {
            showAction("Удаляю очередной товар");
            clickFirstVisible(removeButton);
        }
    }

    private void scrollToBottom() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long previousHeight = 0;

        // Несколько шагов дают ленивой загрузке время показать карточки ниже страницы.
        for (int step = 0; step < 8; step++) {
            showAction("Прокручиваю страницу вниз");
            js.executeScript("window.scrollBy(0, Math.floor(window.innerHeight * 0.8));");
            long currentHeight = ((Number) js.executeScript("return document.body.scrollHeight")).longValue();
            if (currentHeight == previousHeight) {
                break;
            }
            previousHeight = currentHeight;
        }
    }

    private void clickFirstVisible(By locator) {
        wait.until(driver -> driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed));
        List<WebElement> elements = driver.findElements(locator);
        WebElement element = elements.stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new IllegalStateException("Не найден видимый элемент: " + locator));
        click(element);
    }

    private void click(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", element);
        new Actions(driver).moveToElement(element).click().perform();
    }

    private void showAction(String message) {
        if (demoMode) {
            System.out.println("[DEMO] " + message);
            pause();
        }
    }

    private void pause() {
        if (!demoMode) {
            return;
        }

        try {
            Thread.sleep(1800);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Демонстрационный прогон был прерван", exception);
        }
    }

    private String quote(String text) {
        return "'" + text.replace("'", "\\'") + "'";
    }
}
