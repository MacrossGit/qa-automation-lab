package onliner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Простые модульные тесты настроек: браузер и сеть не требуются. */
class TestRunSettingsTest {

    @Test
    void enablesHeadlessModeWhenValueIsTrue() {
        TestRunSettings settings = new TestRunSettings("true");

        assertTrue(settings.isHeadless());
    }

    @Test
    void keepsBrowserVisibleForFalseOrMissingValue() {
        assertFalse(new TestRunSettings("false").isHeadless());
        assertFalse(new TestRunSettings(null).isHeadless());
    }
}
