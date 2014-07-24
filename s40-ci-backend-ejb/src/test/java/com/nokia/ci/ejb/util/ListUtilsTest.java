/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.util;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jajuutin
 */
public class ListUtilsTest {

    @Test
    public void fiftyToTen() {
        runTest(50, 10);
    }

    @Test
    public void fiveToTwo() {
        runTest(5, 2);
    }

    @Test
    public void twoToTen() {
        runTest(2, 10);
    }

    @Test
    public void zeroToTen() {
        runTest(0, 10);
    }

    @Test
    public void tenToZero() {
        runTest(10, 0);
    }

    private void runTest(int sourceDataSize, int partitionSize) {
        // setup
        List<Integer> sourceList = createTestData(sourceDataSize);

        // run
        List<List<Integer>> lists = ListUtils.partition(sourceList, partitionSize);

        // verify
        verifyResult(sourceList, lists, partitionSize);
    }

    /**
     * Create array of integers. element value == index value.
     *
     * example: createTestData(5) will output {0, 1, 2, 3, 4};
     *
     * @param size
     * @return
     */
    private List<Integer> createTestData(int size) {
        List<Integer> testData = new ArrayList<Integer>();
        for (int i = 0; i < size; i++) {
            testData.add(i);
        }
        return testData;
    }

    private int getValidSublistCount(int amountOfAll, int partitionSize) {
        if (partitionSize == 0) {
            return 0;
        }

        int tmp = amountOfAll % partitionSize;

        if (tmp == 0) {
            return amountOfAll / partitionSize;
        }

        return (amountOfAll / partitionSize) + 1;
    }

    private int getLastPartitionSize(List<Integer> sourceList, int partitionSize) {
        if (sourceList.isEmpty()) {
            return 0;
        }

        if (sourceList.size() % partitionSize == 0) {
            return partitionSize;
        }

        int fullPartitions = sourceList.size() / partitionSize;
        return (sourceList.size() - (partitionSize * fullPartitions));
    }

    private void verifyResult(List<Integer> sourceList, List<List<Integer>> output, int partitionSize) {
        // verify amount of sublists.
        int subListCount = getValidSublistCount(sourceList.size(), partitionSize);
        Assert.assertEquals(output.size(), subListCount);
        if (subListCount == 0) {
            return;
        }

        // verify contents of sublists and partition sizes.
        int itemCount = 0;
        for (int i = 0; i < output.size(); i++) {
            List<Integer> list = output.get(i);

            for (int j = 0; j < list.size(); j++) {
                Assert.assertEquals(list.get(j), sourceList.get((i * partitionSize) + j));
                itemCount++;
            }

            if (i == output.size() - 1) {
                // last item might not be full size.
                getLastPartitionSize(sourceList, partitionSize);
            } else {
                Assert.assertEquals(list.size(), partitionSize);
            }
        }

        // verify overall item count.
        Assert.assertEquals(sourceList.size(), itemCount);
    }
}
