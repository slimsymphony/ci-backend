/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.tas;

import com.google.gson.Gson;
import com.nokia.ci.ejb.util.HttpUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author jajuutin
 */
public class TasReader {

    public static List<TasDevice> getTasDevices(String serverUrl, int connectionTimeout, int socketTimeout) throws TasDeviceReadException {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
        HttpConnectionParams.setSoTimeout(params, socketTimeout);
        HttpClient client = HttpUtil.getHttpClient(params);
        HttpGet method = new HttpGet(serverUrl);
        HttpResponse response = null;

        try {
            response = client.execute(method);
        } catch (IOException ex) {
            throw new TasDeviceReadException(ex.getMessage());
        }

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new TasDeviceReadException("Tas reading failed. Received error code:"
                    + Integer.toString(response.getStatusLine().getStatusCode()));
        }

        Gson gson = new Gson();
        TasDeviceList tds = null;
        try {
            tds = gson.fromJson(EntityUtils.toString(response.getEntity()), TasDeviceList.class);
        } catch (IOException ex) {
            throw new TasDeviceReadException(ex.getMessage());
        } catch (IllegalStateException ex) {
            throw new TasDeviceReadException(ex.getMessage());
        }

        if (tds == null || tds.getProducts() == null) {
            return new ArrayList<TasDevice>();
        }

        return tds.getProducts();
    }
}
