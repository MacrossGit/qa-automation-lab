package saucedemo;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Единый E2E-демо-сценарий: путь покупателя от каталога до очистки корзины. */
class ShopperJourneyE2ETest extends BaseUiTest {
    /** Все карточки из каталога, включая распроданные товары для проверки корректного пропуска. */
    private static final List<String> CATALOG_PRODUCTS = List.of(
            "Black heels",
            "Bronze sandals",
            "Brown Shades",
            "Grey jacket",
            "Noir jacket",
            "Striped top",
            "White sandals"
    );

    /**
     * Покупатель просматривает все товары, меняет доступные варианты, добавляет позиции в наличии
     * и в конце очищает корзину. Оформление и оплата намеренно не выполняются.
     */
    @Test
    void shopperExploresCatalogAddsProductsAndClearsCart() {
        SauceDemoPage page = new SauceDemoPage(driver, wait);
        TestCustomer customer = TestCustomer.randomCustomer();
        List<String> addedProducts = new ArrayList<>();
        boolean cartCleared = false;

        try {
            page.openHomePage();
            page.openRegistrationPage();
            boolean accountWasCreated = page.registerNewCustomer(customer);
            if (accountWasCreated) {
                assertTrue(page.isAuthenticated(), "После регистрации покупатель должен войти в личный кабинет");
            }

            page.openCatalog();
            page.scrollDownAndBackUp();

            // Цикл проходит все карточки каталога; изменение select выполняется только когда select существует.
            for (String product : CATALOG_PRODUCTS) {
                page.openProduct(product);
                page.selectAlternativeVariantsIfAvailable();
                if (page.addOpenedProductToCartIfAvailable(product)) {
                    addedProducts.add(product);
                }
            }

            page.openCart();
            assertTrue(!addedProducts.isEmpty(), "В корзину должен быть добавлен хотя бы один товар в наличии");
            assertTrue(addedProducts.stream().allMatch(page::cartContains),
                    "В корзине должны быть все товары, которые доступны для добавления");

            page.clearCart();
            cartCleared = true;
            assertTrue(page.isCartEmpty(), "В конце E2E-сценария корзина должна быть пустой");
        } finally {
            // Если промежуточная проверка упала, всё равно не оставляем товары в тестовой корзине.
            if (!cartCleared) {
                page.clearCart();
            }
        }
    }
}
