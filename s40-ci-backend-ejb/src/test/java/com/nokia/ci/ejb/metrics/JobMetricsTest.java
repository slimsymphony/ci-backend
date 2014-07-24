/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import com.nokia.ci.ejb.EJBTestBase;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.util.Order;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author jajuutin
 */
public class JobMetricsTest extends EJBTestBase {

    private JobMetricsEJB testSubject;
    private final Long JOB_ID = 1L;
    
    @Before
    @Override
    public void before() {
        super.before();
        testSubject = new JobMetricsEJB();
        testSubject.metricsQueryEJB = Mockito.mock(MetricsQueryEJB.class);
    }

    @Test
    public void getIndividualBuilds() throws NotFoundException {
        // setup.
        List<MetricsVerification> metricsVerifications = new ArrayList<MetricsVerification>();

        for (int i = 0; i < 10; i++) {
            BuildStatus buildStatus = null;
            /**
             * 1. UNSTABLE (==PASSED) 2. SUCCESS(==PASSED) 3. FAILURE (==FAILED)
             * 4. SUCCESS(==PASSED) 5. FAILURE (==FAILED) 6. FAILURE (==FAILED)
             * 7. SUCCESS(==PASSED) 8. FAILURE (==FAILED) 9. FAILURE (==FAILED)
             * 10. SUCCESS (==PASSED)
             */
            if (i == 0) {
               buildStatus = BuildStatus.UNSTABLE;
            } else if (i % 2 == 0) {
                buildStatus = BuildStatus.SUCCESS;
            } else {
                buildStatus = BuildStatus.FAILURE;
            }            
            
            MetricsVerification mv = new MetricsVerification(Long.valueOf(i), buildStatus, new Date(), new Date());
            metricsVerifications.add(mv);
        }

        // start or end has no matter in this test case.
        Date start = new Date();
        start.setTime(0);
        Date end = new Date();
        end.setTime(5000);

        Mockito.when(testSubject.metricsQueryEJB.getCompletedVerifications(Mockito.eq(JOB_ID),
                (Date) Mockito.anyObject(), (Date) Mockito.anyObject(), Mockito.eq(Order.ASC))).thenReturn(metricsVerifications);

        // run
        MetricsVerificationGroup mvg = testSubject.getVerifications(1L, start, end, getTimeZone());

        // verify
        Assert.assertEquals(metricsVerifications.size(), mvg.getItems().size());
        for (int i = 0; i < metricsVerifications.size(); i++) {
            MetricsVerification mv = mvg.getItems().get(i);
            MetricsVerification mv2 = metricsVerifications.get(i);
            Assert.assertEquals(mv.getId(), mv2.getId());
        }
        Assert.assertEquals(5, mvg.getPassedCount());
        Assert.assertEquals(5, mvg.getFailedCount());
        Assert.assertEquals(50, Math.round(mvg.getPassrate()));
    }

    @Test
    public void getBuildAverageDaily() throws NotFoundException {
        // setup.
        List<MetricsVerification> metricsVerifications = new ArrayList<MetricsVerification>();
        final int days = 4;
        final int buildsPerDay = 8;

        Calendar baseCal = Calendar.getInstance(getTimeZone());
        baseCal.clear();
        baseCal.set(Calendar.YEAR, 2012);
        baseCal.set(Calendar.MONTH, Calendar.JANUARY);
        baseCal.set(Calendar.DAY_OF_MONTH, 1);
        baseCal.set(Calendar.HOUR_OF_DAY, 1);
        baseCal.set(Calendar.MINUTE, 0);
        baseCal.set(Calendar.SECOND, 0);
        baseCal.set(Calendar.MILLISECOND, 0);

        long[] summarizedRuntimes = new long[days];

        Calendar incremental = (Calendar) baseCal.clone();

        for (int day = 0; day < days; day++) {
            long buildDuration = 1; // seconds

            for (int hour = 1; hour < (buildsPerDay + 1); hour++) {
                incremental.set(Calendar.HOUR_OF_DAY, hour);

                MetricsVerification mv = new MetricsVerification(Long.valueOf(day + 1), null, 
                        incremental.getTime(), new Date(incremental.getTime().getTime() + buildDuration * 1000));
                        
                summarizedRuntimes[day] = summarizedRuntimes[day] + buildDuration * 1000;

                metricsVerifications.add(mv);

                buildDuration++;
            }

            incremental.add(Calendar.DAY_OF_YEAR, 1);
        }

        long[] runtimeAverages = new long[days];
        for (int day = 0; day < days; day++) {
            runtimeAverages[day] = summarizedRuntimes[day] / buildsPerDay;
        }

        Date firstBuildStart = metricsVerifications.get(0).getStartTime();
        Date lastBuildEnd = metricsVerifications.get(metricsVerifications.size() - 1).getEndTime();

        Mockito.when(testSubject.metricsQueryEJB.getCompletedVerifications(Mockito.eq(JOB_ID),
                (Date) Mockito.anyObject(), (Date) Mockito.anyObject(),
                (Order) Mockito.anyObject())).thenReturn(metricsVerifications);

        // Run        
        List<MetricsVerificationGroup> mvgs = testSubject.getVerifications(JOB_ID,
                firstBuildStart, lastBuildEnd, MetricsTimespan.DAILY, getTimeZone());

        // Verify
        Assert.assertEquals(days, mvgs.size());
        long overallResultBuildsCount = 0;
        for (int day = 0; day < days; day++) {
            MetricsVerificationGroup mvg = mvgs.get(day);
            Assert.assertEquals(buildsPerDay, mvg.getItems().size());
            Assert.assertEquals(mvg.getRuntimeAverage(), runtimeAverages[day]);
            Assert.assertEquals(mvg.getSummarizedRuntime(), summarizedRuntimes[day]);
            overallResultBuildsCount = overallResultBuildsCount + mvg.getItems().size();
        }

        Assert.assertEquals(metricsVerifications.size(), overallResultBuildsCount);
    }

    @Test
    public void getBuildAverageDailyNoBuilds() throws NotFoundException {
        // setup.
        final int days = 5;

        Calendar baseCal = Calendar.getInstance(getTimeZone());
        baseCal.clear();
        baseCal.set(Calendar.YEAR, 2012);
        baseCal.set(Calendar.MONTH, Calendar.JANUARY);
        baseCal.set(Calendar.DAY_OF_MONTH, 1);
        baseCal.set(Calendar.HOUR_OF_DAY, 1);
        baseCal.set(Calendar.MINUTE, 0);
        baseCal.set(Calendar.SECOND, 0);
        baseCal.set(Calendar.MILLISECOND, 0);

        Date startDate = baseCal.getTime();
        baseCal.add(Calendar.DAY_OF_YEAR, days - 1);
        Date endDate = baseCal.getTime();

        Mockito.when(testSubject.metricsQueryEJB.getCompletedVerifications(Mockito.eq(JOB_ID),
                (Date) Mockito.anyObject(), (Date) Mockito.anyObject(),
                (Order) Mockito.anyObject())).thenReturn(new ArrayList<MetricsVerification>());

        // Run
        List<MetricsVerificationGroup> mvgs = testSubject.getVerifications(JOB_ID,
                startDate, endDate, MetricsTimespan.DAILY, getTimeZone());

        // Verify
        Assert.assertEquals(days, mvgs.size());
        for (int day = 0; day < days; day++) {
            MetricsVerificationGroup mvg = mvgs.get(day);
            Assert.assertTrue(mvg.getItems().isEmpty());
        }
    }

    @Test
    public void getBuildAverageWeekly() throws NotFoundException {
        // setup.
        List<MetricsVerification> metricsVerifications = new ArrayList<MetricsVerification>();
        final int weeks = 4;
        final int buildsPerWeek = 2;

        Calendar baseCal = Calendar.getInstance(getTimeZone());
        baseCal.clear();
        baseCal.set(Calendar.YEAR, 2012);
        baseCal.set(Calendar.HOUR_OF_DAY, 1);
        baseCal.set(Calendar.MINUTE, 0);
        baseCal.set(Calendar.SECOND, 0);
        baseCal.set(Calendar.MILLISECOND, 0);
        baseCal.set(Calendar.WEEK_OF_YEAR, 1);

        long[] summarizedRuntimes = new long[weeks];

        Calendar incremental = (Calendar) baseCal.clone();

        for (int week = 0; week < weeks; week++) {
            long buildDuration = 1; // seconds                       

            for (int day = 2; day < (buildsPerWeek + 2); day++) {
                incremental.set(Calendar.DAY_OF_WEEK, day);

                MetricsVerification mv = new MetricsVerification(Long.valueOf((day + 1) * day), null,
                        incremental.getTime(), new Date(incremental.getTime().getTime() + buildDuration * 1000));

                summarizedRuntimes[week] = summarizedRuntimes[week] + buildDuration * 1000;

                metricsVerifications.add(mv);

                buildDuration++;
            }

            incremental.add(Calendar.WEEK_OF_YEAR, 1);
        }

        long[] runtimeAverages = new long[weeks];
        for (int week = 0; week < weeks; week++) {
            runtimeAverages[week] = summarizedRuntimes[week] / buildsPerWeek;
        }

        Date firstBuildStart = metricsVerifications.get(0).getStartTime();
        Date lastBuildEnd = metricsVerifications.get(metricsVerifications.size() - 1).getEndTime();

        Mockito.when(testSubject.metricsQueryEJB.getCompletedVerifications(Mockito.eq(JOB_ID),
                (Date) Mockito.anyObject(), (Date) Mockito.anyObject(),
                (Order) Mockito.anyObject())).thenReturn(metricsVerifications);

        // Run
        List<MetricsVerificationGroup> mvgs = testSubject.getVerifications(JOB_ID,
                firstBuildStart, lastBuildEnd, MetricsTimespan.WEEKLY, getTimeZone());

        // Verify
        Assert.assertEquals(weeks, mvgs.size());
        long overallResultBuildsCount = 0;
        for (int week = 0; week < weeks; week++) {
            MetricsVerificationGroup mvg = mvgs.get(week);
            Assert.assertEquals(buildsPerWeek, mvg.getItems().size());
            Assert.assertEquals(mvg.getRuntimeAverage(), runtimeAverages[week]);
            Assert.assertEquals(mvg.getSummarizedRuntime(), summarizedRuntimes[week]);
            overallResultBuildsCount = overallResultBuildsCount + mvg.getItems().size();
        }

        Assert.assertEquals(metricsVerifications.size(), overallResultBuildsCount);
    }

    @Test
    public void getBuildAverageMonthly() throws NotFoundException {
        // setup.
        List<MetricsVerification> metricsVerifications = new ArrayList<MetricsVerification>();
        final int months = 3;
        final int buildsPerMonth = 2;

        Calendar baseCal = Calendar.getInstance(getTimeZone());
        baseCal.clear();
        baseCal.set(Calendar.YEAR, 2012);
        baseCal.set(Calendar.MONTH, Calendar.JANUARY);
        baseCal.set(Calendar.DAY_OF_MONTH, 1);
        baseCal.set(Calendar.HOUR_OF_DAY, 1);
        baseCal.set(Calendar.MINUTE, 0);
        baseCal.set(Calendar.SECOND, 0);
        baseCal.set(Calendar.MILLISECOND, 0);

        long[] summarizedRuntimes = new long[months];

        Calendar incremental = (Calendar) baseCal.clone();

        for (int month = 0; month < months; month++) {
            long buildDuration = 1; // seconds                       

            for (int week = 1; week < (buildsPerMonth + 1); week++) {
                incremental.add(Calendar.DAY_OF_YEAR, 1);

                MetricsVerification mv = new MetricsVerification(Long.valueOf((month + 1) * week), null,
                        incremental.getTime(), new Date(incremental.getTime().getTime() + buildDuration * 1000));

                summarizedRuntimes[month] = summarizedRuntimes[month] + buildDuration * 1000;

                metricsVerifications.add(mv);

                buildDuration++;
            }

            incremental.set(Calendar.DAY_OF_MONTH, 1);
            incremental.add(Calendar.MONTH, 1);
        }

        long[] runtimeAverages = new long[months];
        for (int month = 0; month < months; month++) {
            runtimeAverages[month] = summarizedRuntimes[month] / buildsPerMonth;
        }

        Date firstBuildStart = metricsVerifications.get(0).getStartTime();
        Date lastBuildEnd = metricsVerifications.get(metricsVerifications.size() - 1).getEndTime();

        Mockito.when(testSubject.metricsQueryEJB.getCompletedVerifications(Mockito.eq(JOB_ID),
                (Date) Mockito.anyObject(), (Date) Mockito.anyObject(),
                (Order) Mockito.anyObject())).thenReturn(metricsVerifications);

        // Run
        List<MetricsVerificationGroup> mvgs = testSubject.getVerifications(JOB_ID,
                firstBuildStart, lastBuildEnd, MetricsTimespan.MONTHLY, getTimeZone());

        // Verify
        Assert.assertEquals(months, mvgs.size());
        long overallResultBuildsCount = 0;
        for (int month = 0; month < months; month++) {
            MetricsVerificationGroup mbg = mvgs.get(month);
            Assert.assertEquals(buildsPerMonth, mbg.getItems().size());
            Assert.assertEquals(mbg.getRuntimeAverage(), runtimeAverages[month]);
            Assert.assertEquals(mbg.getSummarizedRuntime(), summarizedRuntimes[month]);
            overallResultBuildsCount = overallResultBuildsCount + mbg.getItems().size();
        }

        Assert.assertEquals(metricsVerifications.size(), overallResultBuildsCount);
    }

    @Test
    public void getBuildBreaks() throws NotFoundException {
        // setup.
        List<MetricsVerification> metricsVerifications = new ArrayList<MetricsVerification>();

        Calendar baseCal = Calendar.getInstance(getTimeZone());
        baseCal.clear();
        baseCal.set(Calendar.YEAR, 2012);
        baseCal.set(Calendar.MONTH, Calendar.JANUARY);
        baseCal.set(Calendar.DAY_OF_MONTH, 1);
        baseCal.set(Calendar.HOUR_OF_DAY, 1);
        baseCal.set(Calendar.MINUTE, 0);
        baseCal.set(Calendar.SECOND, 0);
        baseCal.set(Calendar.MILLISECOND, 0);

        Date start = baseCal.getTime();

        for (int i = 0; i < 8; i++) {
            BuildStatus buildStatus = null;            

            if (i == 0) {
                buildStatus = BuildStatus.UNSTABLE;
            } else if (i == 1) {
                buildStatus = BuildStatus.SUCCESS;
            } else if (i == 2) {
                buildStatus = BuildStatus.SUCCESS;
            } else if (i == 3) {
                buildStatus = BuildStatus.FAILURE;
            } else if (i == 4) {
                buildStatus = BuildStatus.ABORTED;
            } else if (i == 5) {
                buildStatus = BuildStatus.SUCCESS;
            } else if (i == 6) {
                buildStatus = BuildStatus.SUCCESS;
            } else if (i == 7) {
                buildStatus = BuildStatus.FAILURE;
            }

            Calendar endCalendar = (Calendar)baseCal.clone();
            endCalendar.add(Calendar.MINUTE, 30);            
            MetricsVerification mv = new MetricsVerification(Long.valueOf(i), buildStatus, baseCal.getTime(), endCalendar.getTime());
            metricsVerifications.add(mv);

            baseCal.add(Calendar.MINUTE, 60);
        }

        Date end = baseCal.getTime();

        Mockito.when(testSubject.metricsQueryEJB.getCompletedVerifications(JOB_ID, start, end, Order.ASC)).thenReturn(metricsVerifications);

        // run
        List<MetricsVerificationGroup> mvgs = testSubject.getVerificationBreaks(1L, start, end);

        // verify
        Assert.assertEquals(4, mvgs.size());
        Assert.assertTrue(mvgs.get(0).getItems().get(0).getId().equals(metricsVerifications.get(0).getId()));
        Assert.assertTrue(mvgs.get(0).getItems().get(1).getId().equals(metricsVerifications.get(1).getId()));
        Assert.assertTrue(mvgs.get(0).getItems().get(2).getId().equals(metricsVerifications.get(2).getId()));
        Assert.assertTrue(mvgs.get(1).getItems().get(0).getId().equals(metricsVerifications.get(3).getId()));
        Assert.assertTrue(mvgs.get(1).getItems().get(1).getId().equals(metricsVerifications.get(4).getId()));
        Assert.assertTrue(mvgs.get(2).getItems().get(0).getId().equals(metricsVerifications.get(5).getId()));
        Assert.assertTrue(mvgs.get(2).getItems().get(1).getId().equals(metricsVerifications.get(6).getId()));
        Assert.assertTrue(mvgs.get(3).getItems().get(0).getId().equals(metricsVerifications.get(7).getId()));
        Assert.assertEquals(mvgs.get(0).getStartTime(), start);
        Assert.assertEquals(mvgs.get(mvgs.size() - 1).getEndTime(), end);
    }

    @Test
    public void getBuildBreaksNoBuilds() throws NotFoundException {
        // setup.

        // start or end has no matter in this test case.
        Date start = new Date();
        start.setTime(0);
        Date end = new Date();
        end.setTime(5000);

        List<MetricsVerification> metricsVerifications = new ArrayList<MetricsVerification>();
        Mockito.when(testSubject.metricsQueryEJB.getCompletedVerifications(JOB_ID, start, end, Order.ASC)).thenReturn(metricsVerifications);

        // run
        List<MetricsVerificationGroup> mvgs = testSubject.getVerificationBreaks(1L, start, end);

        // verify
        Assert.assertEquals(1, mvgs.size());
        Assert.assertTrue(mvgs.get(0).getItems().isEmpty());
        Assert.assertEquals(mvgs.get(0).getSummarizedRuntime(), 0);
        Assert.assertEquals(mvgs.get(0).getRuntimeAverage(), 0);
        Assert.assertEquals(mvgs.get(0).getStartTime(), start);
        Assert.assertEquals(mvgs.get(0).getEndTime(), end);
    }

    private TimeZone getTimeZone() {
        return Calendar.getInstance().getTimeZone();
    }
}
