package com.nokia.ci.integration;

import java.io.FileInputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test base class for integration test cases.
 * @author vrouvine
 */
public abstract class CITestBase {
    
    private static Logger log = LoggerFactory.getLogger(CITestBase.class);
    /**
     * Base URL.
     */
    protected static String API_BASE_URL = "http://localhost:8888/s40ci/api/";
    /**
     * JDBC URL for test database.
     */
    protected static String JDBC_URL = "jdbc:h2:tcp://localhost:9020/mem:testDB";
    
    protected static String DATA_SET_BASE = "com/nokia/ci/integration/testdata/";
    /**
     * Database connection.
     */
    protected static IDatabaseConnection connection;
    /**
     * Test DataSet.
     */
    protected static IDataSet dataset;
    /**
     * Dataset resource string.
     */
    protected String dataSetResource = "full_data.xml";

    public CITestBase() {
    }

    public CITestBase(String dataSetResource) {
        this.dataSetResource = dataSetResource;
    }

    /**
     * Initializes class.
     * @throws Exception initialization fails.
     */
    @BeforeClass
    public static void init() throws Exception {
        // System properties are added in maven fail-safe plugin configuration.
        String url = System.getProperty("baseUrl");
        if (url != null) {
            API_BASE_URL = url;
        }
        log.info("Using api url: " + API_BASE_URL);
        
        String jdbcUrl = System.getProperty("testDBJdbcUrl");
        if (jdbcUrl != null) {
            JDBC_URL = jdbcUrl;
        }
        JDBC_URL += ";REFERENTIAL_INTEGRITY=0";
        log.info("Using jdbc url: " + JDBC_URL);
        setUpDatabaseConnection();
    }

    /**
     * Closes database connection when class is destroyed.
     * @throws SQLException closing database connection fails.
     */
    @AfterClass
    public static void destroy() throws SQLException {
        closeDatabaseConnection();
    }

    /**
     * Setup called before every test.
     * Cleans database and inserts test data from dataSetResource.
     * @throws Exception setting up test fails
     */
    @Before
    public void setUp() throws Exception {
        formDataSetFromResource(dataSetResource);
        cleanAndInsert();
    }

    /**
     * Sets up database connection to test database.
     * @throws Exception database connection setup fails.
     */
    protected static void setUpDatabaseConnection() throws Exception {
        log.info("Setting up database connection...");
        Class.forName("org.h2.Driver");
        Connection jdbcConnection = DriverManager.getConnection(JDBC_URL,
                "test", "test");
        alterSequence(jdbcConnection);
        connection = new DatabaseConnection(jdbcConnection);
        connection.getConfig().setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, true);
        connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
    }
    
    protected static void alterSequence(Connection jdbcConnection) throws SQLException {
        PreparedStatement prepareStatement = jdbcConnection.prepareStatement("alter sequence S40CICORE.HIBERNATE_SEQUENCE restart with 1000");
        prepareStatement.execute();
    }

    /**
     * Closes database connection.
     * @throws SQLException closing connection fails.
     */
    protected static void closeDatabaseConnection() throws SQLException {
        log.info("Closing database connection...");
        if (connection == null) {
            return;
        }
        connection.close();
    }

    /**
     * Forms flat XML dataset from given resource.
     * @param resource given resource
     * @return {@link IDataSet} object
     * @throws Exception forming dataset fails.
     */
    protected void formDataSetFromResource(String resource) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource(DATA_SET_BASE + resource);
        dataset = new FlatXmlDataSetBuilder().build(new FileInputStream(url.getFile()));
    }

    /**
     * Cleans database and inserts data from given resource.
     * @param resource test data resource.
     * @throws Exception database action fails.
     */
    protected void cleanAndInsert() throws Exception {
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataset);
    }

    /**
     * Parses id string from location header URL.
     * @param locationUrl HTTP location header URL
     * @return id string
     */
    protected String parseIdFromLocationUrl(String locationUrl) {
        return locationUrl.substring(locationUrl.lastIndexOf("/") + 1, locationUrl.length());
    }
}
