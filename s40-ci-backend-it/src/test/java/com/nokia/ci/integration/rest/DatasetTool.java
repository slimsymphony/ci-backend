/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.integration.rest;

import junit.framework.Assert;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;

/**
 *
 * @author jajuutin
 */
public class DatasetTool {

    public static String JOB_TABLE_NAME = "S40CICORE.JOB";
    public static String JOB_VERIFICATION_CONF_TABLE_NAME = "S40CICORE.JOB_VERIFICATION_CONF";
    public static String PRODUCT_TABLE_NAME = "S40CICORE.PRODUCT";
    public static String VERIFICATION_TABLE_NAME = "S40CICORE.VERIFICATION";
    public static String BRANCH_TABLE_NAME = "S40CICORE.BRANCH";
    public static String BUILD_TABLE_NAME = "S40CICORE.BUILD";
    public static String PROJECT_TABLE_NAME = "S40CICORE.PROJECT";
    public static String PROJECT_PRODUCT_TABLE_NAME = "S40CICORE.PROJECT_PRODUCT";
    public static String PROJECT_VERIFICATION_TABLE_NAME = "S40CICORE.PROJECT_VERIFICATION";
    public static String PROJECT_VERIFICATION_CONF_TABLE_NAME = "S40CICORE.PROJECT_VERIFICATION_CONF";
    public static String INCIDENT_TABLE_NAME = "S40CICORE.INCIDENT";

    private DatasetTool() {
    }

    public static String getDatasetValue(IDataSet dataset, String table,
            int row, String column) {
        String value = null;

        try {
            value = (String) dataset.getTable(table).getValue(row, column);
        } catch (DataSetException ex) {
            Assert.assertTrue(false);
        }

        return value;
    }

    public static int getRowCount(IDataSet dataset, String table) {
        int count = -1;

        try {
            count = dataset.getTable(table).getRowCount();
        } catch (DataSetException ex) {
            Assert.assertTrue(false);
        }

        return count;
    }
}
