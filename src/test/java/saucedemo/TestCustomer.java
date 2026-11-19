package saucedemo;

import java.util.UUID;

/**
 * Данные нового покупателя для одного прогона E2E.
 * Запись хранится только в памяти теста и создаётся заново при каждом запуске.
 */
record TestCustomer(String firstName, String lastName, String email, String password) {
    /**
     * Создаёт уникальные данные, которые не конфликтуют с предыдущими прогонами.
     * UUID не является персональными данными и подходит для тестового адреса.
     *
     * @return новый случайный тестовый покупатель
     */
    static TestCustomer randomCustomer() {
        String uniquePart = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return new TestCustomer(
                "Aqa" + uniquePart.substring(0, 4),
                "Student" + uniquePart.substring(4, 8),
                // example.com — зарезервированный, но синтаксически валидный домен для учебных данных.
                "aqa-" + uniquePart + "@example.com",
                "Demo!" + uniquePart
        );
    }
}
