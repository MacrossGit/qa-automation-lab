package saucedemo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Независимые проверки каталога и загрузки контента. */
class CatalogTest extends BaseUiTest {
    /** Проверяет, что каталог показывает товары магазина. */
    @Test
    void catalogContainsProducts() {
        SauceDemoPage page = new SauceDemoPage(driver, wait);

        page.openCatalog();

        assertTrue(page.catalogProductCount() >= 7,
                "В демо-каталоге должны отображаться все семь доступных товаров");
    }

    /** Проверяет, что после прокрутки вниз и обратно товарные карточки остаются доступны. */
    @Test
    void catalogRemainsAvailableAfterScrollingDownAndUp() {
        SauceDemoPage page = new SauceDemoPage(driver, wait);

        page.openCatalog();
        page.scrollDownAndBackUp();

        assertTrue(page.catalogProductCount() >= 7,
                "После прокрутки каталог не должен потерять отображаемые товары");
    }
}
