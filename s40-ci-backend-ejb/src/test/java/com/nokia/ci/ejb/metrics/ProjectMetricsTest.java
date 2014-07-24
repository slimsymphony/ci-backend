/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import com.nokia.ci.ejb.EJBTestBase;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.ChangeTracker;
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
public class ProjectMetricsTest extends EJBTestBase {

    private ProjectMetricsEJB testSubject;
    private final Long PROJECT_ID = 1L;

    @Before
    @Override
    public void before() {
        super.before();
        testSubject = new ProjectMetricsEJB();
        testSubject.metricsQueryEJB = Mockito.mock(MetricsQueryEJB.class);
    }

    @Test
    public void getIndividualDevelopmentHangtime() throws NotFoundException {
        // setup.
        List<ChangeTracker> cts = new ArrayList<ChangeTracker>();

        long summarizedLen = 0;
        final long ITEM_COUNT = 10;
        // Create 10 change trackers with development hangtimes of 1000-10000ms
        for (int i = 0; i < ITEM_COUNT; i++) {
            ChangeTracker ct = new ChangeTracker();
            ct.setCommitId(Integer.toString(i));

            ct.setScvStart(new Date(0));
            long len = 100 * (i + 1);
            ct.setDbvEnd(new Date(len));

            summarizedLen += len;

            cts.add(ct);
        }

        // start or end has no matter in this test case.
        Date start = new Date();
        start.setTime(0);
        Date end = new Date();
        end.setTime(10000);

        Mockito.when(testSubject.metricsQueryEJB.getChangeTrackers(Mockito.eq(PROJECT_ID),
                (Date) Mockito.anyObject(), (Date) Mockito.anyObject(),
                Mockito.eq(MetricsHangtimeType.DEVELOPMENT))).thenReturn(cts);

        // run
        MetricsHangtimeGroup mhg = testSubject.getHangtimes(1L, start, end,
                getTimeZone(), MetricsHangtimeType.DEVELOPMENT);

        // verify
        Assert.assertEquals(cts.size(), mhg.getItemCount());
        for (int i = 0; i < cts.size(); i++) {
            Assert.assertEquals(cts.get(i).getCommitId(), mhg.getItems().get(i).getCommitId());
            Assert.assertEquals(new Long(cts.get(i).getDbvEnd().getTime() - cts.get(i).getScvStart().getTime()),
                    mhg.getItems().get(i).getHangtime());
        }
        Assert.assertEquals(new Long(summarizedLen / ITEM_COUNT), mhg.getHangtimeAverage());
    }

    @Test
    public void getIntegrationHangtimeDaily() throws NotFoundException {
        // setup.

        List<ChangeTracker> cts = new ArrayList<ChangeTracker>();
        final int days = 4;
        final int trackersPerDay = 8;

        Calendar baseCal = Calendar.getInstance(getTimeZone());
        baseCal.clear();
        baseCal.set(Calendar.YEAR, 2012);
        baseCal.set(Calendar.MONTH, Calendar.JANUARY);
        baseCal.set(Calendar.DAY_OF_MONTH, 1);
        baseCal.set(Calendar.HOUR_OF_DAY, 1);
        baseCal.set(Calendar.MINUTE, 0);
        baseCal.set(Calendar.SECOND, 0);
        baseCal.set(Calendar.MILLISECOND, 0);

        long[] summarizedHangtimes = new long[days];

        Calendar incremental = (Calendar) baseCal.clone();

        for (int day = 0; day < days; day++) {
            long duration = 1; // seconds

            for (int hour = 1; hour < (trackersPerDay + 1); hour++) {
                incremental.set(Calendar.HOUR_OF_DAY, hour);

                ChangeTracker ct = new ChangeTracker();

                ct.setDbvEnd(incremental.getTime());
                ct.setReleased(new Date(incremental.getTime().getTime() + duration * 1000));

                summarizedHangtimes[day] = summarizedHangtimes[day] + duration * 1000;

                cts.add(ct);

                duration++;
            }

            incremental.add(Calendar.DAY_OF_YEAR, 1);
        }

        long[] hangtimeAverages = new long[days];
        for (int day = 0; day < days; day++) {
            hangtimeAverages[day] = summarizedHangtimes[day] / trackersPerDay;
        }

        Date firstTrackerStart = cts.get(0).getDbvEnd();
        Date lastTrackerStart = cts.get(cts.size() - 1).getReleased();

        Mockito.when(testSubject.metricsQueryEJB.getChangeTrackers(Mockito.eq(PROJECT_ID),
                (Date) Mockito.anyObject(), (Date) Mockito.anyObject(),
                Mockito.eq(MetricsHangtimeType.INTEGRATION))).thenReturn(cts);

        // Run        
        List<MetricsHangtimeGroup> mhgs = testSubject.getHangtimes(PROJECT_ID,
                firstTrackerStart, lastTrackerStart, MetricsTimespan.DAILY, getTimeZone(),
                MetricsHangtimeType.INTEGRATION);

        // Verify
        Assert.assertEquals(days, mhgs.size());
        long overallCount = 0;
        for (int day = 0; day < days; day++) {
            MetricsHangtimeGroup mvg = mhgs.get(day);
            Assert.assertEquals(trackersPerDay, mvg.getItems().size());
            Assert.assertEquals(mvg.getHangtimeAverage(), new Long(hangtimeAverages[day]));
            overallCount = overallCount + mvg.getItems().size();
        }

        Assert.assertEquals(cts.size(), overallCount);
    }

    private TimeZone getTimeZone() {
        return Calendar.getInstance().getTimeZone();
    }
}
