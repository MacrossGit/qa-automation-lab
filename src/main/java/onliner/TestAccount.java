package onliner;

import io.github.cdimascio.dotenv.Dotenv;

/** Учётные данные для E2E-теста. Значения передаются только из окружения. */
public record TestAccount(String email, String password) {
    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    public static TestAccount fromEnvironment() {
        return new TestAccount(
                valueOf("ONLINER_EMAIL"),
                valueOf("ONLINER_PASSWORD")
        );
    }

    /** Авторизационный тест запускается, только когда заполнены оба значения. */
    public boolean isConfigured() {
        return email != null && !email.isBlank()
                && password != null && !password.isBlank();
    }

    /** Системная переменная важнее значения из .env — это удобно для CI. */
    private static String valueOf(String name) {
        String environmentValue = System.getenv(name);
        return environmentValue == null || environmentValue.isBlank()
                ? DOTENV.get(name)
                : environmentValue;
    }
}
