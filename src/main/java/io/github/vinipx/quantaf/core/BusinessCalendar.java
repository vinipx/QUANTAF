package io.github.vinipx.quantaf.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Business calendar that supports weekends and configurable holidays.
 * Used for settlement date calculations (T+1, T+2, etc.).
 * <p>
 * Supports multiple market calendars (NYSE, LSE, TSE) via factory methods.
 */
public class BusinessCalendar {

    private static final Logger log = LoggerFactory.getLogger(BusinessCalendar.class);
    private static final Set<DayOfWeek> WEEKEND = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    private final String name;
    private final Set<LocalDate> holidays;
    private final Set<MonthDay> recurringHolidays;

    public BusinessCalendar() {
        this("DEFAULT", Set.of(), Set.of());
    }

    public BusinessCalendar(String name, Set<LocalDate> holidays, Set<MonthDay> recurringHolidays) {
        this.name = name;
        this.holidays = new HashSet<>(holidays);
        this.recurringHolidays = new HashSet<>(recurringHolidays);
    }

    /**
     * Creates a NYSE (New York Stock Exchange) calendar with standard US holidays.
     */
    public static BusinessCalendar nyse() {
        Set<MonthDay> recurring = Set.of(
                MonthDay.of(1, 1),   // New Year's Day
                MonthDay.of(7, 4),   // Independence Day
                MonthDay.of(12, 25)  // Christmas Day
        );
        return new BusinessCalendar("NYSE", Set.of(), recurring);
    }

    /**
     * Creates a LSE (London Stock Exchange) calendar with standard UK holidays.
     */
    public static BusinessCalendar lse() {
        Set<MonthDay> recurring = Set.of(
                MonthDay.of(1, 1),   // New Year's Day
                MonthDay.of(12, 25), // Christmas Day
                MonthDay.of(12, 26)  // Boxing Day
        );
        return new BusinessCalendar("LSE", Set.of(), recurring);
    }

    /**
     * Creates a TSE (Tokyo Stock Exchange) calendar with standard Japanese holidays.
     */
    public static BusinessCalendar tse() {
        Set<MonthDay> recurring = Set.of(
                MonthDay.of(1, 1),   // New Year's Day
                MonthDay.of(1, 2),   // Bank Holiday
                MonthDay.of(1, 3),   // Bank Holiday
                MonthDay.of(12, 31)  // New Year's Eve
        );
        return new BusinessCalendar("TSE", Set.of(), recurring);
    }

    /**
     * Adds a specific holiday date.
     */
    public BusinessCalendar withHoliday(LocalDate date) {
        this.holidays.add(date);
        return this;
    }

    /**
     * Adds a recurring holiday (same month/day every year).
     */
    public BusinessCalendar withRecurringHoliday(MonthDay monthDay) {
        this.recurringHolidays.add(monthDay);
        return this;
    }

    /**
     * Checks if a given date is a business day.
     */
    public boolean isBusinessDay(LocalDate date) {
        if (WEEKEND.contains(date.getDayOfWeek())) {
            return false;
        }
        if (holidays.contains(date)) {
            return false;
        }
        MonthDay md = MonthDay.from(date);
        return !recurringHolidays.contains(md);
    }

    /**
     * Adds the specified number of business days to the given date.
     * Skips weekends and holidays.
     *
     * @param startDate    the start date
     * @param businessDays the number of business days to add (must be >= 0)
     * @return the resulting business date
     */
    public LocalDate addBusinessDays(LocalDate startDate, int businessDays) {
        if (businessDays < 0) {
            throw new IllegalArgumentException("Business days must be non-negative");
        }
        LocalDate date = startDate;
        int added = 0;
        while (added < businessDays) {
            date = date.plusDays(1);
            if (isBusinessDay(date)) {
                added++;
            }
        }
        log.trace("Added {} business days to {}: result={} [calendar={}]", businessDays, startDate, date, name);
        return date;
    }

    /**
     * Calculates the number of business days between two dates (exclusive of end date).
     */
    public int businessDaysBetween(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        int count = 0;
        LocalDate date = start;
        while (date.isBefore(end)) {
            date = date.plusDays(1);
            if (isBusinessDay(date)) {
                count++;
            }
        }
        return count;
    }

    public String getName() {
        return name;
    }
}
