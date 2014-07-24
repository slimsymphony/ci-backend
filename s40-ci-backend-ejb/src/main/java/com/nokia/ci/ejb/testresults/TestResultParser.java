/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.testresults;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.nokia.ci.ejb.util.HttpUtil;
import com.nokia.ci.ejb.util.MathUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author hhellgre
 */
public class TestResultParser {

    private static final Logger log = LoggerFactory.getLogger(TestResultParser.class);
    private File file;
    private URL url;
    private int socketTimeout = 5000;
    private int connectionTimeout = 5000;

    public TestResultParser(String file, URL url, int socketTimeout, int connectionTimeout) {
        this(file, url);
        this.socketTimeout = socketTimeout;
        this.connectionTimeout = connectionTimeout;
    }

    public TestResultParser(String file, URL url) {
        this.file = new File(file);
        this.url = url;
    }

    public TestResultParser(String file) {
        this.file = new File(file);
    }

    public TestResultParser(URL url) {
        this.url = url;
    }

    public List<NJUnitTestSuite> parseNJUnit() {
        List<NJUnitTestSuite> ret = new ArrayList<NJUnitTestSuite>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = null;
            if (file != null && file.exists() && file.canRead()
                    && file.getAbsoluteFile().toString().contains(".xml")) {
                log.info("Parsing test NJUnit result file {}", file.getAbsoluteFile());
                doc = dBuilder.parse(file);
            } else if (url != null && url.toString().contains(".xml")) {
                log.info("Parsing test NJUnit result url {}", url);
                InputStream is = getHttpInputStream();
                if (is != null) {
                    doc = dBuilder.parse(is);
                }
            }

            if (doc == null) {
                return ret;
            }

            doc.getDocumentElement().normalize();

            NodeList suites = doc.getElementsByTagName("testsuite");

            for (int i = 0; i < suites.getLength(); i++) {
                Node suite = suites.item(i);

                if (suite.getNodeType() == Node.ELEMENT_NODE) {
                    int failedCases = 0;
                    int naCases = 0;

                    Element e = (Element) suite;
                    NJUnitTestSuite testSuite = new NJUnitTestSuite();

                    if (e.hasAttribute("name")) {
                        testSuite.setName(e.getAttribute("name"));
                    }

                    Map<String, String> props = new HashMap<String, String>();

                    Element propertyNode = (Element) e.getElementsByTagName("properties").item(0);
                    if (propertyNode != null) {
                        NodeList properties = propertyNode.getElementsByTagName("property");
                        for (int j = 0; j < properties.getLength(); j++) {
                            Element property = (Element) properties.item(j);
                            props.put(property.getAttribute("name"), property.getAttribute("value"));
                        }
                    }

                    testSuite.setProperties(props);

                    List<NJUnitTestCase> tests = new ArrayList<NJUnitTestCase>();
                    NodeList testCases = e.getElementsByTagName("testcase");
                    testSuite.setNumTestcases(testCases.getLength());
                    for (int j = 0; j < testCases.getLength(); j++) {
                        Element test = (Element) testCases.item(j);
                        NJUnitTestCase testCase = new NJUnitTestCase();

                        if (test.hasAttribute("classname")) {
                            testCase.setClassname(test.getAttribute("classname"));
                        }

                        if (test.hasAttribute("name")) {
                            testCase.setName(test.getAttribute("name"));
                        }

                        NodeList reports = test.getElementsByTagName("report");
                        if (reports.getLength() > 0) {
                            Element report = (Element) reports.item(0);
                            if (report.hasAttribute("relativePath")) {
                                String path = report.getAttribute("relativePath");
                                testCase.setRelativePath(path);
                            }
                        }

                        NodeList childs = test.getElementsByTagName("failure");
                        if (childs.getLength() > 0) {
                            failedCases += childs.getLength();
                            List<NJUnitTestFailure> fails = new ArrayList<NJUnitTestFailure>();
                            for (int k = 0; k < childs.getLength(); k++) {
                                Element failure = (Element) childs.item(k);
                                NJUnitTestFailure f = new NJUnitTestFailure();
                                if (failure.hasAttribute("type")) {
                                    f.setType(failure.getAttribute("type"));
                                } else if (failure.hasAttribute("message")) {
                                    f.setType(failure.getAttribute("message"));
                                }

                                if (failure.hasAttribute("detail")) {
                                    f.setMessage(failure.getAttribute("detail"));
                                } else {
                                    f.setMessage(failure.getTextContent());
                                }
                                fails.add(f);
                            }
                            testCase.setFailures(fails);

                            // Move failed cases to first
                            tests.add(0, testCase);
                            continue;
                        }

                        childs = test.getElementsByTagName("na");
                        if (childs.getLength() > 0) {
                            naCases += childs.getLength();
                            List<NJUnitTestNA> notAllowed = new ArrayList<NJUnitTestNA>();
                            for (int k = 0; k < childs.getLength(); k++) {
                                Element failure = (Element) childs.item(k);
                                NJUnitTestNA f = new NJUnitTestNA();
                                if (failure.hasAttribute("type")) {
                                    f.setType(failure.getAttribute("type"));
                                } else if (failure.hasAttribute("message")) {
                                    f.setType(failure.getAttribute("message"));
                                }

                                if (failure.hasAttribute("detail")) {
                                    f.setMessage(failure.getAttribute("detail"));
                                } else {
                                    f.setMessage(failure.getTextContent());
                                }
                                notAllowed.add(f);
                            }
                            testCase.setNotAllowed(notAllowed);

                            // Move NA cases to first
                            tests.add(0, testCase);
                            continue;
                        }

                        tests.add(testCase);
                    }

                    testSuite.setNumFailures(failedCases);
                    testSuite.setNumNA(naCases);
                    testSuite.setNumSuccess(testSuite.getNumTestcases() - testSuite.getNumFailures() - testSuite.getNumNA());

                    if (testSuite.getNumTestcases() > 0) {
                        testSuite.setSuccessPercent(MathUtil.getPercentage(testSuite.getNumSuccess(), testSuite.getNumTestcases()));
                        testSuite.setFailPercent(MathUtil.getPercentage(testSuite.getNumFailures(), testSuite.getNumTestcases()));
                        testSuite.setNApercent(MathUtil.getPercentage(testSuite.getNumNA(), testSuite.getNumTestcases()));
                    }

                    testSuite.setTestCases(tests);
                    ret.add(testSuite);
                }
            }

        } catch (Exception e) {
            log.warn("Could not parse NJUnit test file/URL {} {}", file, url);
        }

        return ret;
    }

    public MemUsageObject parseMemUsageJSON() {
        MemUsageObject model = null;

        Gson gson = new Gson();
        BufferedReader br = getJsonReader();
        log.info("Trying to parse mem usage JSON, file: {}, url: {}", file, url);

        if (br != null) {
            try {
                model = gson.fromJson(br, MemUsageObject.class);
            } catch (JsonSyntaxException ex) {
                log.warn("Could not parse Json file: {}, url: {}", file, url);
            } catch (JsonIOException ex) {
                log.warn("Could not parse Json file: {}, url: {}", file, url);
            }

            // Dirty hack to check that we have atleast something good in the file
            if (model != null && model.getComponents().isEmpty()
                    && model.getLibraries().isEmpty() && model.getObjects().isEmpty()) {
                log.info("Mem usage JSON is not in correct format! file: {}, url: {}", file, url);
                return null;
            }
        }

        return model;
    }

    public WarningObject parseWarnings() {
        WarningObject ret = null;

        Gson gson = new Gson();
        BufferedReader br = getJsonReader();

        log.info("Trying to parse warnings JSON, file: {}, url: {}", file, url);
        if (br != null) {
            try {
                ret = gson.fromJson(br, WarningObject.class);
            } catch (JsonSyntaxException ex) {
                log.warn("Could not parse Json file: {}, url: {}", file, url);
            } catch (JsonIOException ex) {
                log.warn("Could not parse Json file: {}, url: {}", file, url);
            }

            // Dirty hack to check that we have atleast something good in the file
            if (ret != null && ret.getWarnings().isEmpty()) {
                log.info("Warnings JSON is not in correct format! file: {}, url: {}", file, url);
                return null;
            }
        }

        return ret;
    }

    private BufferedReader getJsonReader() {
        BufferedReader br = null;

        try {
            if (file != null && file.exists() && file.canRead()
                    && file.getAbsoluteFile().toString().contains(".json")) {
                br = new BufferedReader(new FileReader(file));
            } else if (url != null && url.toString().contains(".xml")) {
                InputStream is = getHttpInputStream();
                if (is != null) {
                    br = new BufferedReader(new InputStreamReader(is));
                }
            }
        } catch (Exception e) {
            log.debug("Could not parse Warnings result file/URL {} {}", file, url);
        }
        return br;
    }

    private InputStream getHttpInputStream() throws IOException {
        InputStream is = null;
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
        HttpConnectionParams.setSoTimeout(params, socketTimeout);
        HttpClient client = HttpUtil.getHttpClient(params);

        HttpGet method = new HttpGet(url.toString());
        HttpResponse response = client.execute(method);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            is = response.getEntity().getContent();
        }
        return is;
    }
}
