package org.san.home.phsorter;

import junit.framework.TestCase;

import java.time.LocalDate;

/**
 * Created by User on 20.05.2018.
 */
public class DatePeriodTest extends TestCase {

    private static final String SINGLE_DAY = "02.14 (14 февр)";
    private static final String PERIOD = "03.09-12 (отпуск)";
    private static final String MONTH = "09.хх (дома)";

    public void testInstanceOf() throws Exception {
        //2012/02.14 (14 февр)
        //2015/03.09-12 (отпуск)
        //2017/09.хх (дома)
        assertTrue(DatePeriod.instanceOf(SINGLE_DAY));
        assertTrue(DatePeriod.instanceOf(PERIOD));
        assertTrue(DatePeriod.instanceOf(MONTH));
        assertFalse(DatePeriod.instanceOf("2012"));
    }

    public void testGetType() throws Exception {
        assertEquals(DatePeriod.DatePeriodType.SINGLE_DAY, DatePeriod.getType(SINGLE_DAY));
        assertEquals(DatePeriod.DatePeriodType.PERIOD, DatePeriod.getType(PERIOD));
        assertEquals(DatePeriod.DatePeriodType.MONTH, DatePeriod.getType(MONTH));
    }

    public void testOf() throws Exception {
        //2012/02.14 (14 февр)
        assertEquals(
                new DatePeriod(LocalDate.of(2012, 2, 14), LocalDate.of(2012, 2, 14), DatePeriod.DatePeriodType.SINGLE_DAY),
                DatePeriod.of("2012", SINGLE_DAY)
        );
        //2015/03.09-12 (СафариПарк)
        assertEquals(
                new DatePeriod(LocalDate.of(2012, 3, 9), LocalDate.of(2012, 3, 12), DatePeriod.DatePeriodType.PERIOD),
                DatePeriod.of("2012", PERIOD)
        );
        //2017/09.хх (Оля)
        assertEquals(
                new DatePeriod(LocalDate.of(2012, 9, 1), LocalDate.of(2012, 9, 28), DatePeriod.DatePeriodType.MONTH),
                DatePeriod.of("2012", MONTH)
        );
    }

}