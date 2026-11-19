package saucedemo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Независимые пользовательские сценарии для корзины. */
class CartTest extends BaseUiTest {
    /** Добавляет товар в корзину и проверяет его присутствие. */
    @Test
    void shopperCanAddProductToCart() {
        SauceDemoPage page = new SauceDemoPage(driver, wait);

        page.openProduct("Grey jacket");
        page.addOpenedProductToCart("Grey jacket");
        page.openCart();

        assertTrue(page.cartContains("Grey jacket"), "Добавленный товар должен быть в корзине");
    }

    /** Добавляет товар и очищает корзину пользовательскими кликами. */
    @Test
    void shopperCanClearCart() {
        SauceDemoPage page = new SauceDemoPage(driver, wait);

        page.openProduct("Noir jacket");
        page.addOpenedProductToCart("Noir jacket");
        page.clearCart();

        assertTrue(page.isCartEmpty(), "После удаления всех товаров корзина должна быть пустой");
    }
}
