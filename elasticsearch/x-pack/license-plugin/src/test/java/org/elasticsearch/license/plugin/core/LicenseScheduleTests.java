/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.license.plugin.core;

import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.license.core.License;
import org.elasticsearch.license.plugin.TestUtils;
import org.elasticsearch.test.ESTestCase;
import org.junit.Before;

import static org.hamcrest.Matchers.equalTo;

public class LicenseScheduleTests extends ESTestCase {

    private License license;
    private LicenseSchedule schedule;

    @Before
    public void setuo() throws Exception {
        license = TestUtils.generateSignedLicense(TimeValue.timeValueHours(12));
        schedule = new LicenseSchedule(license);
    }

    public void testEnabledLicenseSchedule() throws Exception {
        int expiryDuration = (int) (license.expiryDate() - license.issueDate());
        long triggeredTime = license.issueDate() + between(0, expiryDuration);
        assertThat(schedule.nextScheduledTimeAfter(license.issueDate(), triggeredTime), equalTo(license.expiryDate()));
    }

    public void testGraceLicenseSchedule() throws Exception {
        long triggeredTime = license.expiryDate() + between(1,
                ((int) LicenseState.GRACE_PERIOD_DURATION.getMillis()));
        assertThat(schedule.nextScheduledTimeAfter(license.issueDate(), triggeredTime),
                equalTo(license.expiryDate() + LicenseState.GRACE_PERIOD_DURATION.getMillis()));
    }

    public void testExpiredLicenseSchedule() throws Exception {
        long triggeredTime = license.expiryDate() + LicenseState.GRACE_PERIOD_DURATION.getMillis() +
                randomIntBetween(1, 1000);
        assertThat(schedule.nextScheduledTimeAfter(license.issueDate(), triggeredTime),
                equalTo(-1L));
    }

    public void testInvalidLicenseSchedule() throws Exception {
        long triggeredTime = license.issueDate() - randomIntBetween(1, 1000);
        assertThat(schedule.nextScheduledTimeAfter(triggeredTime, triggeredTime),
                equalTo(license.issueDate()));
    }
}
