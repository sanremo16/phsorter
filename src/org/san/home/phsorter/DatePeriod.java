package org.san.home.phsorter;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Период времени
 * Created by User on 04.04.2018.
 */
public class DatePeriod implements Comparable {
    private static final Logger logger = LoggerFactory.getLogger(DatePeriod.class);
    private static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");

    public LocalDate startDate;

    public LocalDate endDate;

    public DatePeriodType getType() {
        return type;
    }

    public DatePeriodType type;

    enum DatePeriodType {
        //02.14
        SINGLE_DAY(Pattern.compile("(\\d{2}).(\\d{2})(.*)")),
        //02.01-05
        PERIOD(Pattern.compile("(\\d{2}).(\\d{2})-(\\d{2})(.*)")),
        //02.XX || 02.xx
        MONTH(Pattern.compile("(\\d{2}).(xx|XX|хх|ХХ)(.*)")),
        UNKNOWN(null);

        private Pattern pattern;

        DatePeriodType(Pattern p) {
            pattern = p;
        }

        @Nullable
        public Pattern getPattern() {
            return  pattern;
        }
    };


    DatePeriod(@NotNull LocalDate start, @NotNull LocalDate end, DatePeriodType t) {
        startDate = start;
        endDate = end;
        type = t;
    }

    static boolean instanceOf(String fileName) {
        return DatePeriodType.PERIOD.getPattern().matcher(fileName).matches()
                || DatePeriodType.SINGLE_DAY.getPattern().matcher(fileName).matches()
                || DatePeriodType.MONTH.getPattern().matcher(fileName).matches();
    }

    static DatePeriodType getType(String fileName) {
        if (DatePeriodType.PERIOD.getPattern().matcher(fileName).matches()) {
            return DatePeriodType.PERIOD;
        } else if (DatePeriodType.SINGLE_DAY.getPattern().matcher(fileName).matches()) {
            return DatePeriodType.SINGLE_DAY;
        } else if (DatePeriodType.MONTH.getPattern().matcher(fileName).matches()) {
            return DatePeriodType.MONTH;
        } else {
            logger.error("Unknown DatePeriod type ", fileName);
            return DatePeriodType.UNKNOWN;
        }
    }

    /**
     * Получение периода времени
     * @param year -  в виде 2012
     * @param period -  в виде строк вида 2015/03.09-12 (СафариПарк)
     * @return
     */
    static DatePeriod of(String year, String period) {
        //2012/02.14 (14 февр)
        //2015/03.09-12 (СафариПарк)
        //2017/09.хх (Оля)
        DatePeriodType datePeriodType = getType(period);
        Matcher matcher;
        switch (datePeriodType) {
            case SINGLE_DAY:
                matcher = DatePeriodType.SINGLE_DAY.getPattern().matcher(period);
                matcher.find();
                LocalDate startDate = LocalDate.of(parseInt(year), parseInt(matcher.group(1)), parseInt(matcher.group(2)));
                return new DatePeriod(startDate, startDate, datePeriodType);
            case PERIOD:
                matcher = DatePeriodType.PERIOD.getPattern().matcher(period);
                matcher.find();
                return new DatePeriod(
                        LocalDate.of(parseInt(year), parseInt(matcher.group(1)), parseInt(matcher.group(2))),
                        LocalDate.of(parseInt(year), parseInt(matcher.group(1)), parseInt(matcher.group(3))),
                        datePeriodType);
            case MONTH:
                matcher = DatePeriodType.MONTH.getPattern().matcher(period);
                matcher.find();
                return new DatePeriod(
                        LocalDate.of(parseInt(year), parseInt(matcher.group(1)), 1),
                        //FixMe!!!
                        LocalDate.of(parseInt(year), parseInt(matcher.group(1)), 28),
                        datePeriodType);
            case UNKNOWN:
            default:
                throw new IllegalStateException("Couldn't create DatePeriod of year =" + year +", period = " + period);
        }
    }

    public int compareTo(Object o) {
        if (o instanceof DatePeriod) {
            return startDate.compareTo(((DatePeriod) o).startDate);
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DatePeriod) {
            DatePeriod target = (DatePeriod) obj;
            return startDate.equals(target.getStartDate())
                    && endDate.equals(target.getEndDate())
                    && type.equals(target.getType());
        } else {
            return false;
        }
    }

    public boolean contain(LocalDate localDate) {
        return startDate.equals(localDate) ||
                endDate.equals(localDate) ||
                (startDate.isBefore(localDate) && endDate.isAfter(localDate));
    }

    private static Integer parseInt(@NotNull String str) {
        return str.startsWith("0") ? new Integer(str.substring(1)) : new Integer(str);
    }

    static boolean isYear(String str) {
        return YEAR_PATTERN.matcher(str).matches();
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

}
