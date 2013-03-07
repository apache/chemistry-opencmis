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
package org.apache.chemistry.opencmis.commons.impl;

import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeHelper {

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private static final Pattern XML_DATETIME = Pattern
            .compile("(\\d{4,9})-([01]\\d)-([0-3]\\d)T([0-2]\\d):([0-5]\\d):([0-5]\\d)(\\.(\\d+))?(([+-][0-2]\\d:[0-5]\\d)|Z)?");
    private static final BigDecimal BD1000 = new BigDecimal(1000);

    private static final String[] HTTP_DATETIME = new String[] { "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd-MMM-yy HH:mm:ss zzz", "EEE MMM d HH:mm:ss yyyy" };

    private static final ThreadLocal<SoftReference<SimpleDateFormat[]>> THREADLOCAL_HTTP_FORMATS = new ThreadLocal<SoftReference<SimpleDateFormat[]>>() {
        @Override
        protected SoftReference<SimpleDateFormat[]> initialValue() {
            return new SoftReference<SimpleDateFormat[]>(new SimpleDateFormat[HTTP_DATETIME.length]);
        }
    };

    /**
     * Parses a xsd:dateTime string.
     */
    public static GregorianCalendar parseXmlDateTime(String s) throws IllegalArgumentException {
        if (s == null) {
            return null;
        }

        Matcher m = XML_DATETIME.matcher(s);

        if (!m.matches()) {
            return null;
        }

        try {
            int year = Integer.parseInt(m.group(1));
            int month = Integer.parseInt(m.group(2));
            int day = Integer.parseInt(m.group(3));
            int hour = Integer.parseInt(m.group(4));
            int minute = Integer.parseInt(m.group(5));
            int second = Integer.parseInt(m.group(6));
            int millisecond = 0;

            if (m.group(8) != null) {
                millisecond = (new BigDecimal("0." + m.group(8))).multiply(BD1000).intValue();
            }

            TimeZone tz = GMT;

            if (m.group(10) != null) {
                tz = TimeZone.getTimeZone("GMT" + m.group(10));
            }

            GregorianCalendar result = new GregorianCalendar();
            result.clear();

            result.setTimeZone(tz);
            result.set(year, month - 1, day, hour, minute, second);
            result.set(Calendar.MILLISECOND, millisecond);

            return result;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Returns a xsd:dateTime string.
     */
    public static String formatXmlDateTime(long millis) {
        GregorianCalendar cal = new GregorianCalendar(GMT);
        cal.setTimeInMillis(millis);

        return formatXmlDateTime(cal);
    }

    /**
     * Returns a xsd:dateTime string.
     */
    public static String formatXmlDateTime(GregorianCalendar cal) {
        if (cal == null) {
            throw new IllegalArgumentException();
        }

        StringBuilder sb = new StringBuilder(String.format("%04d-%02d-%02dT%02d:%02d:%02d", cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND)));

        int ms = cal.get(Calendar.MILLISECOND);
        if (ms > 0) {
            StringBuilder mssb = new StringBuilder(String.format("%03d", ms));
            while (mssb.charAt(mssb.length() - 1) == '0') {
                mssb.deleteCharAt(mssb.length() - 1);
            }
            sb.append(".");
            sb.append(mssb);
        }

        int tz = cal.getTimeZone().getRawOffset();
        if (tz == 0) {
            sb.append("Z");
        } else {
            if (tz > 0) {
                sb.append("+");
            } else {
                sb.append("-");
                tz *= -1;
            }
            sb.append(String.format("%02d", tz / 3600000));
            sb.append(":");
            int tzm = tz % 3600000;
            sb.append(String.format("%02d", tzm == 0 ? 0 : tzm / 60000));
        }

        return sb.toString();
    }

    /**
     * Parses a HTTP date.
     */
    public static Date parseHttpDateTime(String s) {
        if (s == null) {
            return null;
        }

        s = s.trim();
        if (s.length() > 1 && s.startsWith("'") && s.endsWith("'")) {
            s = s.substring(1, s.length() - 1);
        }

        for (int i = 0; i < HTTP_DATETIME.length; i++) {
            SimpleDateFormat sdf = getFormatter(i);

            try {
                return sdf.parse(s);
            } catch (ParseException e) {
                // try next
            }
        }

        return null;
    }

    /**
     * Returns a HTTP date.
     */
    public static String formateHttpDateTime(long millis) {
        return getFormatter(0).format(millis);
    }

    /**
     * Returns a HTTP date.
     */
    public static String formateHttpDateTime(Date date) {
        return getFormatter(0).format(date);
    }

    /**
     * Returns a HTTP date.
     */
    public static String formateHttpDateTime(GregorianCalendar cal) {
        return getFormatter(0).format(cal.getTimeInMillis());
    }

    /**
     * Clears out cached formatters.
     */
    public static void clear() {
        THREADLOCAL_HTTP_FORMATS.remove();
    }

    private static SimpleDateFormat getFormatter(int x) {
        SoftReference<SimpleDateFormat[]> ref = THREADLOCAL_HTTP_FORMATS.get();
        SimpleDateFormat[] sdfs = ref.get();
        if (sdfs == null) {
            ref = new SoftReference<SimpleDateFormat[]>(new SimpleDateFormat[HTTP_DATETIME.length]);
            THREADLOCAL_HTTP_FORMATS.set(ref);
            sdfs = ref.get();
        }

        SimpleDateFormat sdf = sdfs[x];
        if (sdf == null) {
            sdf = new SimpleDateFormat(HTTP_DATETIME[x], Locale.US);
            sdf.setTimeZone(GMT);
            sdfs[x] = sdf;
        }

        return sdf;
    }
}
