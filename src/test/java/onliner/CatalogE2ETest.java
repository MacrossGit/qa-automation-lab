package onliner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Базовые пользовательские сценарии каталога, не выполняющие оформление заказа. */
class CatalogE2ETest extends BaseUiTest {

    @Test
    void searchShowsRelevantResults() {
        OnlinerPage page = new OnlinerPage(driver, wait);
        page.openHomePage();
        page.searchFor("iPhone 16");

        assertTrue(page.searchValue().equals("iPhone 16"),
                "В поле поиска должен быть введён поисковый запрос");
    }

    @Test
    void userCanChangeDeliveryCity() {
        OnlinerPage page = new OnlinerPage(driver, wait);
        page.openTestProduct();
        page.openCitySelector();
        page.chooseCity("Брест");

        assertTrue(page.pageText().contains("Брест"),
                "После выбора город Брест должен отображаться на странице");
    }

    @Test
    void userCanAddAndRemoveProductFromCart() {
        OnlinerPage page = new OnlinerPage(driver, wait);
        page.openTestProduct();
        page.addFirstOfferToCart();
        page.openCart();

        assertTrue(page.pageText().contains("Xiaomi"),
                "Добавленный товар должен быть в корзине");

        page.removeFirstItemFromCart();
        assertTrue(page.pageText().contains("пуста") || page.pageText().contains("Пуст"),
                "После удаления корзина должна быть пустой");
    }
}
