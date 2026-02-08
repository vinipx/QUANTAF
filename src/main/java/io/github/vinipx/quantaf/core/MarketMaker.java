package io.github.vinipx.quantaf.core;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * The Market Maker: Generates realistic financial test data using statistical distributions.
 * <p>
 * All methods are thread-safe. Uses {@link ThreadLocalRandom} for non-distribution
 * operations and Apache Commons Math for statistical distributions.
 */
public class MarketMaker {

    private static final Logger log = LoggerFactory.getLogger(MarketMaker.class);
    private static final MathContext FINANCIAL_PRECISION = new MathContext(10, RoundingMode.HALF_EVEN);

    private final BusinessCalendar calendar;

    public MarketMaker() {
        this(new BusinessCalendar());
    }

    public MarketMaker(BusinessCalendar calendar) {
        this.calendar = calendar;
    }

    /**
     * Generates a price using a Normal (Gaussian) distribution.
     *
     * @param mean   the mean price
     * @param stdDev the standard deviation
     * @return a price sampled from N(mean, stdDev), always positive
     */
    public BigDecimal generatePrice(double mean, double stdDev) {
        if (stdDev < 0) {
            throw new IllegalArgumentException("Standard deviation must be non-negative");
        }
        NormalDistribution distribution = new NormalDistribution(mean, Math.max(stdDev, 1e-10));
        double price = distribution.sample();
        // Ensure positive price
        price = Math.abs(price);
        BigDecimal result = BigDecimal.valueOf(price).round(FINANCIAL_PRECISION);
        log.debug("Generated price: {} (mean={}, stdDev={})", result, mean, stdDev);
        return result;
    }

    /**
     * Generates a trade volume using a Poisson distribution.
     *
     * @param lambda the expected mean volume (must be positive)
     * @return a volume sampled from Poisson(lambda), minimum 1
     */
    public int generateVolume(int lambda) {
        if (lambda <= 0) {
            throw new IllegalArgumentException("Lambda must be positive");
        }
        PoissonDistribution distribution = new PoissonDistribution(lambda);
        int volume = distribution.sample();
        volume = Math.max(volume, 1); // Minimum volume of 1
        log.debug("Generated volume: {} (lambda={})", volume, lambda);
        return volume;
    }

    /**
     * Generates a settlement date adjusted for T+N logic using the business calendar.
     *
     * @param settlementType the settlement type (T0, T1, T2)
     * @return the settlement date, skipping weekends and holidays
     */
    public LocalDate generateTradeDate(SettlementType settlementType) {
        LocalDate tradeDate = LocalDate.now();
        LocalDate settlementDate = calendar.addBusinessDays(tradeDate, settlementType.getDays());
        log.debug("Generated trade date: {} -> settlement: {} ({})", tradeDate, settlementDate, settlementType);
        return settlementDate;
    }

    /**
     * Generates a realistic trade timestamp with random time during market hours (9:30-16:00 ET).
     *
     * @return a trade timestamp during market hours
     */
    public LocalDateTime generateTradeTimestamp() {
        LocalDate today = LocalDate.now();
        int secondsInMarketDay = (int) (6.5 * 3600); // 9:30 to 16:00
        int randomSeconds = ThreadLocalRandom.current().nextInt(secondsInMarketDay);
        LocalTime marketOpen = LocalTime.of(9, 30);
        LocalTime tradeTime = marketOpen.plusSeconds(randomSeconds);
        return LocalDateTime.of(today, tradeTime);
    }

    /**
     * Generates a series of correlated prices using Cholesky decomposition.
     * Useful for simulating correlated instrument movements in portfolio testing.
     *
     * @param mean        the mean price
     * @param stdDev      the standard deviation
     * @param correlation the correlation coefficient between consecutive prices (-1 to 1)
     * @param count       the number of prices to generate
     * @return a list of correlated prices
     */
    public List<BigDecimal> generateCorrelatedPrices(double mean, double stdDev, double correlation, int count) {
        if (correlation < -1 || correlation > 1) {
            throw new IllegalArgumentException("Correlation must be between -1 and 1");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }

        NormalDistribution dist = new NormalDistribution(0, 1);
        double[] independent = IntStream.range(0, count)
                .mapToDouble(i -> dist.sample())
                .toArray();

        List<BigDecimal> prices = new java.util.ArrayList<>(count);
        double previous = independent[0];

        for (int i = 0; i < count; i++) {
            double correlated;
            if (i == 0) {
                correlated = independent[i];
            } else {
                // Apply correlation: Z_i = rho * Z_{i-1} + sqrt(1 - rho^2) * epsilon_i
                correlated = correlation * previous + Math.sqrt(1 - correlation * correlation) * independent[i];
            }
            previous = correlated;
            double price = Math.abs(mean + stdDev * correlated);
            prices.add(BigDecimal.valueOf(price).round(FINANCIAL_PRECISION));
        }

        log.debug("Generated {} correlated prices (mean={}, stdDev={}, corr={})", count, mean, stdDev, correlation);
        return prices;
    }

    /**
     * Generates a random Client Order ID in the format: QUANTAF-{timestamp}-{random}.
     *
     * @return a unique ClOrdID
     */
    public String generateClOrdId() {
        return String.format("QUANTAF-%d-%04d",
                System.currentTimeMillis(),
                ThreadLocalRandom.current().nextInt(10000));
    }

    /**
     * Generates a random account identifier.
     *
     * @param prefix the account prefix (e.g., "ACC", "FUND")
     * @return an account ID like "ACC-12345678"
     */
    public String generateAccountId(String prefix) {
        return String.format("%s-%08d", prefix, ThreadLocalRandom.current().nextInt(100_000_000));
    }

    /**
     * Settlement type for trade date generation.
     */
    public enum SettlementType {
        T0(0),
        T1(1),
        T2(2);

        private final int days;

        SettlementType(int days) {
            this.days = days;
        }

        public int getDays() {
            return days;
        }
    }
}
