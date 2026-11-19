package onliner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Длинный ручной E2E-сценарий для демонстрации каталога.
 * Один метод означает один браузер и одну временную тестовую корзину.
 */
class CatalogDemoE2ETest extends BaseUiTest {

    private static final List<String> PRODUCTS = List.of(
            "POCO X7",
            "Компьютерное кресло",
            "Компьютерный стол",
            "ASUS Vivobook 17",
            "iPhone 16",
            "Samsung Galaxy S25",
            "Робот-пылесос",
            "Стиральная машина",
            "Наушники Sony",
            "Механическая клавиатура"
    );

    @Test
    void userExploresCatalogAddsTenProductsAndClearsCart() {
        // true включает паузы и сообщения [DEMO] для показа работы теста.
        OnlinerPage page = new OnlinerPage(driver, wait, true);
        TestAccount account = TestAccount.fromEnvironment();

        try {
            // Переходим по всем крупным разделам главной страницы и прокручиваем каждый вниз.
            page.exploreMainCatalogs();

            // Вход выполняется после просмотра каталогов и до добавления товаров в корзину.
            Assumptions.assumeTrue(account.isConfigured(),
                    "Для демо задайте ONLINER_EMAIL и ONLINER_PASSWORD");
            page.openLogin();
            page.login(account.email(), account.password());
            // Ищем и добавляем десять разных товаров. Оформление и оплата не выполняются.
            for (String product : PRODUCTS) {
                page.searchAndOpenProduct(product);
                page.addFirstOfferToCart();
            }

            page.openCart();
            assertTrue(page.pageText().contains("Корзин"),
                    "После добавления товаров должна открываться корзина");
        } finally {
            // Очистка выполняется даже при ошибке поиска или добавления товара.
            page.clearCart();
        }
    }
}
