/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.chemistry.opencmis.commons.impl.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.chemistry.opencmis.commons.impl.DateTimeHelper;
import org.junit.Test;

public class DateTimeTest {

    @Test
    public void testXmlDateTimeParser() {
        GregorianCalendar cal1 = DateTimeHelper.parseXmlDateTime("2012-12-24T09:15:06.123Z");
        assertEquals(2012, cal1.get(Calendar.YEAR));
        assertEquals(11, cal1.get(Calendar.MONTH));
        assertEquals(24, cal1.get(Calendar.DAY_OF_MONTH));
        assertEquals(9, cal1.get(Calendar.HOUR_OF_DAY));
        assertEquals(15, cal1.get(Calendar.MINUTE));
        assertEquals(6, cal1.get(Calendar.SECOND));
        assertEquals(123, cal1.get(Calendar.MILLISECOND));
        assertEquals(0, cal1.getTimeZone().getRawOffset());

        GregorianCalendar cal2 = DateTimeHelper.parseXmlDateTime("2013-02-04T23:45:55.9876543");
        assertEquals(2013, cal2.get(Calendar.YEAR));
        assertEquals(1, cal2.get(Calendar.MONTH));
        assertEquals(4, cal2.get(Calendar.DAY_OF_MONTH));
        assertEquals(23, cal2.get(Calendar.HOUR_OF_DAY));
        assertEquals(45, cal2.get(Calendar.MINUTE));
        assertEquals(55, cal2.get(Calendar.SECOND));
        assertEquals(987, cal2.get(Calendar.MILLISECOND));
        assertEquals(0, cal2.getTimeZone().getRawOffset());

        GregorianCalendar cal3 = DateTimeHelper.parseXmlDateTime("2013-01-02T03:04:05.678+05:00");
        assertEquals(2013, cal3.get(Calendar.YEAR));
        assertEquals(0, cal3.get(Calendar.MONTH));
        assertEquals(2, cal3.get(Calendar.DAY_OF_MONTH));
        assertEquals(3, cal3.get(Calendar.HOUR_OF_DAY));
        assertEquals(4, cal3.get(Calendar.MINUTE));
        assertEquals(5, cal3.get(Calendar.SECOND));
        assertEquals(678, cal3.get(Calendar.MILLISECOND));
        assertEquals(5 * 60 * 60 * 1000, cal3.getTimeZone().getRawOffset());
    }

    @Test
    public void testXmlDateTimeWriter() {
        GregorianCalendar cal1 = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal1.set(2012, 11, 24, 9, 10, 11);
        cal1.set(Calendar.MILLISECOND, 0);

        assertEquals("2012-12-24T09:10:11Z", DateTimeHelper.formatXmlDateTime(cal1));

        GregorianCalendar cal2 = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal2.set(2013, 0, 1, 2, 3, 4);
        cal2.set(Calendar.MILLISECOND, 50);

        assertEquals("2013-01-01T02:03:04.05Z", DateTimeHelper.formatXmlDateTime(cal2));

        GregorianCalendar cal3 = new GregorianCalendar(TimeZone.getTimeZone("GMT+05:00"));
        cal3.set(2012, 11, 24, 9, 10, 11);
        cal3.set(Calendar.MILLISECOND, 0);

        assertEquals("2012-12-24T09:10:11+05:00", DateTimeHelper.formatXmlDateTime(cal3));

        GregorianCalendar cal4 = new GregorianCalendar(TimeZone.getTimeZone("GMT-03:02"));
        cal4.set(100, 8, 17, 18, 30, 7);
        cal4.set(Calendar.MILLISECOND, 654);

        assertEquals("0100-09-17T18:30:07.654-03:02", DateTimeHelper.formatXmlDateTime(cal4));

        GregorianCalendar cal5 = new GregorianCalendar(TimeZone.getTimeZone("GMT+01:00"));
        cal5.set(2012, 11, 24, 9, 10, 11);
        cal5.set(Calendar.MILLISECOND, 0);

        assertEquals("2012-12-24T08:10:11Z", DateTimeHelper.formatXmlDateTime(cal5.getTimeInMillis()));

        GregorianCalendar cal6 = new GregorianCalendar(TimeZone.getTimeZone("Europe/Berlin"));
        cal6.set(2013, 4, 6, 9, 10, 11);
        cal6.set(Calendar.MILLISECOND, 0);

        assertEquals("2013-05-06T09:10:11+02:00", DateTimeHelper.formatXmlDateTime(cal6));
        
        GregorianCalendar cal7 = new GregorianCalendar(TimeZone.getTimeZone("Europe/Berlin"));
        cal7.set(2012, 11, 24, 9, 10, 11);
        cal7.set(Calendar.MILLISECOND, 0);

        assertEquals("2012-12-24T09:10:11+01:00", DateTimeHelper.formatXmlDateTime(cal7));
    }

    @Test
    public void testXmlDateTimeWriterAndParser() {
        GregorianCalendar cal1 = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal1.set(2012, 11, 24, 9, 10, 11);
        cal1.set(Calendar.MILLISECOND, 0);

        GregorianCalendar cal2 = DateTimeHelper.parseXmlDateTime(DateTimeHelper.formatXmlDateTime(cal1));

        assertEquals(cal1, cal2);

        GregorianCalendar cal3 = new GregorianCalendar(TimeZone.getTimeZone("GMT+05:00"));
        cal3.set(2012, 11, 24, 9, 10, 11);
        cal3.set(Calendar.MILLISECOND, 12);

        GregorianCalendar cal4 = DateTimeHelper.parseXmlDateTime(DateTimeHelper.formatXmlDateTime(cal3));

        assertEquals(cal3, cal4);
    }

    @Test
    public void testInvalidXmlDateTime() {
        // null is not a date
        assertNull(DateTimeHelper.parseXmlDateTime(null));

        // "" is not a date
        assertNull(DateTimeHelper.parseXmlDateTime(""));

        // wrong format
        assertNull(DateTimeHelper.parseXmlDateTime("1111111111111111"));

        // invalid year
        assertNull(DateTimeHelper.parseXmlDateTime("111-11-11T11:11:11.111Z"));

        // invalid month
        assertNull(DateTimeHelper.parseXmlDateTime("1111-21-11T11:11:11.111Z"));

        // invalid day
        assertNull(DateTimeHelper.parseXmlDateTime("1111-11-51T11:11:11.111Z"));
    }

    @Test
    public void testHttpDateTimeParser() {
        GregorianCalendar cal1 = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal1.set(2012, 11, 24, 9, 10, 11);
        cal1.set(Calendar.MILLISECOND, 0);

        String dateStr1 = DateTimeHelper.formatHttpDateTime(cal1.getTime());
        assertEquals("Mon, 24 Dec 2012 09:10:11 GMT", dateStr1);

        Date date1 = DateTimeHelper.parseHttpDateTime(dateStr1);

        assertEquals(cal1.getTime(), date1);

        GregorianCalendar cal2 = new GregorianCalendar(TimeZone.getTimeZone("GMT+05:00"));
        cal2.set(2013, 0, 1, 2, 3, 4);
        cal2.set(Calendar.MILLISECOND, 0);

        String dateStr2 = "  '" + DateTimeHelper.formatHttpDateTime(cal2.getTimeInMillis()) + "' ";
        Date date2 = DateTimeHelper.parseHttpDateTime(dateStr2);

        assertEquals(cal2.getTime(), date2);

        GregorianCalendar cal3 = new GregorianCalendar(TimeZone.getTimeZone("GMT+05:00"));
        cal3.set(2012, 11, 24, 9, 10, 11);
        cal3.set(Calendar.MILLISECOND, 0);

        String dateStr3 = DateTimeHelper.formatHttpDateTime(cal3);
        Date date3 = DateTimeHelper.parseHttpDateTime(dateStr3);

        assertEquals(cal3.getTime(), date3);

        GregorianCalendar cal4 = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal4.set(1994, 10, 6, 8, 49, 37);
        cal4.set(Calendar.MILLISECOND, 0);

        String dateStr4 = "Sunday, 06-Nov-94 08:49:37 GMT";

        assertEquals(cal4.getTime(), DateTimeHelper.parseHttpDateTime(dateStr4));

        GregorianCalendar cal5 = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal5.set(1994, 10, 6, 8, 49, 37);
        cal5.set(Calendar.MILLISECOND, 0);

        String dateStr5 = "Sun Nov  6 08:49:37 1994";

        assertEquals(cal5.getTime(), DateTimeHelper.parseHttpDateTime(dateStr5));

        GregorianCalendar cal6 = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal6.set(1994, 10, 16, 8, 49, 37);
        cal6.set(Calendar.MILLISECOND, 0);

        String dateStr6 = "Sun Nov 16 08:49:37 1994";

        assertEquals(cal6.getTime(), DateTimeHelper.parseHttpDateTime(dateStr6));
    }

    @Test
    public void testInvalidHttpDateTime() {
        // null is not a date
        assertNull(DateTimeHelper.parseHttpDateTime(null));

        // "" is not a date
        assertNull(DateTimeHelper.parseHttpDateTime(""));

        // wrong format
        assertNull(DateTimeHelper.parseHttpDateTime("1111111111111111"));

        // invalid year
        assertNull(DateTimeHelper.parseHttpDateTime("Mon, 24 Dec abcd 09:10:11 GMT"));

        // invalid month
        assertNull(DateTimeHelper.parseHttpDateTime("Mon, 24 abc 2012 09:10:11 GMT"));

        // invalid day
        assertNull(DateTimeHelper.parseHttpDateTime("Mon, xy Dec 2012 09:10:11 GMT"));
    }
}
