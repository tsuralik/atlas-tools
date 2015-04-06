package com.datatactics.l3.fcc.atlas.solr;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Assert;
import org.junit.Test;

public class QueryResultsCheck {
    
    private void writeURL2Stream(String url, OutputStream fout)
            throws MalformedURLException, IOException {
        URL u = new URL(url);
        HttpURLConnection uc = (HttpURLConnection) u.openConnection();
        InputStream in = new BufferedInputStream(uc.getInputStream());
        Reader r = new InputStreamReader(in);
        int c;

        while ((c = r.read()) != -1) {
            fout.write(c);
        }
    }
    
    private String getURLResponseAsString(String host, String queryString) throws MalformedURLException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeURL2Stream(host+queryString, baos);
        return baos.toString();
    }

    private String trimResponse(String urlResponseAsString) {
        Object obj = JSONValue.parse(urlResponseAsString);
        JSONObject jsonObj = (JSONObject) obj;
        Object responseObj = jsonObj.get("response");
        
        if (responseObj == null) {
            throw new NullPointerException("no response provided in results");
        }
        
        return responseObj.toString();
    }
    
    @Test
    public void test() {
        try {
            String solrHost    = "http://192.255.32.218:8500/solr/ECFS/select";
            String servletHost = "http://192.255.32.218:8080/fccEcfs/Select";
            String queryString = "?q=*%3A*&sort=id+asc&wt=json&indent=true";

            String solrHostResults = trimResponse(getURLResponseAsString(solrHost, queryString));
            String servletHostResults = trimResponse(getURLResponseAsString(servletHost, queryString));

            System.err.println("solrHostResults:\n" + solrHostResults);
            System.err.println("servletHostResults:\n" + servletHostResults);
            
            Assert.assertEquals(solrHostResults, servletHostResults);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
        
    }

}
