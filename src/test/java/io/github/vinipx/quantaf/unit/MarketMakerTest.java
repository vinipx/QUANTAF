package io.github.vinipx.quantaf.unit;

import io.github.vinipx.quantaf.core.BusinessCalendar;
import io.github.vinipx.quantaf.core.MarketMaker;
import io.github.vinipx.quantaf.core.MarketMaker.SettlementType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for MarketMaker statistical data generation.
 * Uses statistical validation with tolerances for distribution-based methods.
 */
public class MarketMakerTest {

    private MarketMaker marketMaker;

    @BeforeMethod
    public void setUp() {
        marketMaker = new MarketMaker();
    }

    @Test
    public void generatePrice_shouldReturnPositiveValue() {
        BigDecimal price = marketMaker.generatePrice(150.0, 5.0);
        assertThat(price).isPositive();
    }

    @Test
    public void generatePrice_shouldHaveMeanWithinTolerance() {
        int sampleSize = 10_000;
        double mean = 100.0;
        double stdDev = 10.0;

        double sum = 0;
        for (int i = 0; i < sampleSize; i++) {
            sum += marketMaker.generatePrice(mean, stdDev).doubleValue();
        }
        double sampleMean = sum / sampleSize;

        // The sample mean should be close to the theoretical mean
        // Using abs because we take absolute value of prices
        assertThat(sampleMean).isCloseTo(mean, within(2.0));
    }

    @Test
    public void generatePrice_shouldRejectNegativeStdDev() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> marketMaker.generatePrice(100.0, -1.0))
                .withMessageContaining("non-negative");
    }

    @Test
    public void generateVolume_shouldReturnPositiveValue() {
        int volume = marketMaker.generateVolume(100);
        assertThat(volume).isGreaterThan(0);
    }

    @Test
    public void generateVolume_shouldHaveMeanWithinTolerance() {
        int sampleSize = 10_000;
        int lambda = 500;

        long sum = 0;
        for (int i = 0; i < sampleSize; i++) {
            sum += marketMaker.generateVolume(lambda);
        }
        double sampleMean = (double) sum / sampleSize;

        assertThat(sampleMean).isCloseTo(lambda, within(10.0));
    }

    @Test
    public void generateVolume_shouldRejectNonPositiveLambda() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> marketMaker.generateVolume(0))
                .withMessageContaining("positive");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> marketMaker.generateVolume(-5));
    }

    @Test
    public void generateTradeDate_T0_shouldReturnToday() {
        LocalDate result = marketMaker.generateTradeDate(SettlementType.T0);
        // T+0 returns today (or next business day if today is not a business day)
        assertThat(result).isAfterOrEqualTo(LocalDate.now().minusDays(1));
    }

    @Test
    public void generateTradeDate_T2_shouldSkipWeekends() {
        // Use a known Friday to verify weekend skipping
        BusinessCalendar calendar = new BusinessCalendar();
        LocalDate friday = LocalDate.of(2026, 2, 6); // A Friday
        LocalDate result = calendar.addBusinessDays(friday, 2);

        // T+2 from Friday should land on Tuesday
        assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
        assertThat(result).isEqualTo(LocalDate.of(2026, 2, 10));
    }

    @Test
    public void generateTradeDate_shouldSkipHolidays() {
        // Dec 25 2026 is a Friday (holiday), Dec 26 is Sat, Dec 27 is Sun
        LocalDate christmas = LocalDate.of(2026, 12, 25);
        BusinessCalendar calendar = new BusinessCalendar("TEST",
                Set.of(christmas),
                Set.of());

        // Thursday Dec 24 + T1 should skip Dec 25 (holiday), Dec 26-27 (weekend), land on Dec 28 (Monday)
        LocalDate dec24 = LocalDate.of(2026, 12, 24);
        LocalDate result = calendar.addBusinessDays(dec24, 1);
        assertThat(result).isEqualTo(LocalDate.of(2026, 12, 28));
    }

    @Test
    public void generateCorrelatedPrices_shouldReturnCorrectCount() {
        List<BigDecimal> prices = marketMaker.generateCorrelatedPrices(100.0, 5.0, 0.8, 50);
        assertThat(prices).hasSize(50);
    }

    @Test
    public void generateCorrelatedPrices_shouldReturnPositiveValues() {
        List<BigDecimal> prices = marketMaker.generateCorrelatedPrices(100.0, 5.0, 0.9, 100);
        assertThat(prices).allSatisfy(p -> assertThat(p).isPositive());
    }

    @Test
    public void generateCorrelatedPrices_shouldRejectInvalidCorrelation() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> marketMaker.generateCorrelatedPrices(100.0, 5.0, 1.5, 10))
                .withMessageContaining("between -1 and 1");
    }

    @Test
    public void generateClOrdId_shouldBeUnique() {
        String id1 = marketMaker.generateClOrdId();
        String id2 = marketMaker.generateClOrdId();

        assertThat(id1).startsWith("QUANTAF-");
        assertThat(id2).startsWith("QUANTAF-");
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    public void generateAccountId_shouldHaveCorrectFormat() {
        String account = marketMaker.generateAccountId("FUND");
        assertThat(account).matches("FUND-\\d{8}");
    }

    @Test
    public void generateTradeTimestamp_shouldBeDuringMarketHours() {
        var timestamp = marketMaker.generateTradeTimestamp();
        int hour = timestamp.getHour();
        assertThat(hour).isBetween(9, 16);
    }

    @Test
    public void nyseCalendar_shouldSkipJuly4th() {
        BusinessCalendar nyse = BusinessCalendar.nyse();
        LocalDate july4 = LocalDate.of(2026, 7, 4);
        assertThat(nyse.isBusinessDay(july4)).isFalse();
    }

    @Test
    public void businessDaysBetween_shouldCountCorrectly() {
        BusinessCalendar calendar = new BusinessCalendar();
        // Monday to Friday = 4 business days
        LocalDate monday = LocalDate.of(2026, 2, 2);
        LocalDate friday = LocalDate.of(2026, 2, 6);
        assertThat(calendar.businessDaysBetween(monday, friday)).isEqualTo(4);
    }

    @Test
    public void recurringHoliday_shouldBeRecognizedEveryYear() {
        BusinessCalendar calendar = new BusinessCalendar("TEST", Set.of(),
                Set.of(MonthDay.of(3, 15)));
        assertThat(calendar.isBusinessDay(LocalDate.of(2026, 3, 15))).isFalse();
        assertThat(calendar.isBusinessDay(LocalDate.of(2027, 3, 15))).isFalse();
    }
}
