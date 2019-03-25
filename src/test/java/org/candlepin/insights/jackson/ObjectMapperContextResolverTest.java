/*
 * Copyright (c) 2009 - 2019 Red Hat, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Red Hat trademarks are not licensed under GPLv3. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package org.candlepin.insights.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.candlepin.insights.ApplicationProperties;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.Calendar;
import java.util.TimeZone;

@TestInstance(Lifecycle.PER_CLASS)
public class ObjectMapperContextResolverTest {

    private ObjectMapper mapper;

    @BeforeAll
    public void setupTests() {
        ApplicationProperties props = new ApplicationProperties();
        ObjectMapperContextResolver resolver = new ObjectMapperContextResolver(props);
        mapper = resolver.getContext(Void.class);
    }

    /**
     * Ensure that dates are in ISO-8601 format.
     */
    @Test
    public void ensureDatesAreSerializedToISO8601Format() throws Exception {

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 12);
        cal.set(Calendar.YEAR, 2019);
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 15);
        cal.set(Calendar.MILLISECOND, 222);

        String formatted = mapper.writeValueAsString(cal.getTime());
        // NOTE: The mapper will wrap the string in quotes.
        assertEquals("\"2019-01-12T08:30:15.222+00:00\"", formatted);

    }

    @Test
    public void serialization() throws Exception {
        String expectedVal1 = "foo";
        String expectedVal2 = "bar";

        TestPojo pojo = new TestPojo(expectedVal1, expectedVal2);
        String data = mapper.writeValueAsString(pojo);
        assertContainsProperty(data, "value1", expectedVal1);
        assertContainsProperty(data, "value2", expectedVal2);
    }

    @Test
    public void ensureSerializedObjectsDoNotIncludePropsWithNullValues() throws Exception {
        String v2 = "bar";
        TestPojo pojo = new TestPojo(null, v2);
        String data = mapper.writeValueAsString(pojo);
        assertDoesNotContainProperty(data, "value1");
        assertContainsProperty(data, "value2", v2);
    }

    @Test
    public void ensureSerializedObjectsDoNotIncludePropsWithEmptyValues() throws Exception {
        String v2 = "bar";
        TestPojo pojo = new TestPojo("", v2);
        String data = mapper.writeValueAsString(pojo);
        assertDoesNotContainProperty(data, "value1");
        assertContainsProperty(data, "value2", v2);
    }

    private void assertContainsProperty(String data, String key, String value)  throws Exception {
        String toFind = String.format("\"%s\":\"%s\"", key, value);
        assertTrue(data.contains(toFind));
    }

    private void assertDoesNotContainProperty(String data, String property) throws Exception {
        assertFalse(data.contains(property));
    }

    private class TestPojo {
        private String value1;
        private String value2;

        public TestPojo() { }

        public TestPojo(String value1, String value2) {
            this.value1 = value1;
            this.value2 = value2;
        }

        public String getValue1() {
            return value1;
        }

        public void setValue1(String value1) {
            this.value1 = value1;
        }

        public String getValue2() {
            return value2;
        }

        public void setValue2(String value2) {
            this.value2 = value2;
        }
    }
}
