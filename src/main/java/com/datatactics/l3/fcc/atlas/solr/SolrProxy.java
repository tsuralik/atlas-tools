package com.datatactics.l3.fcc.atlas.solr;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.common.SolrInputDocument;

import com.datatactics.l3.fcc.utils.LogFormatter;

public class SolrProxy {
    private static Logger log = LogManager.getLogger(SolrProxy.class);
    private static Logger exLogger = LogManager.getLogger("Exceptions");
    
    private static CloudSolrServer solrServer;
    private static String ZOOKEEPERS;
    private static String COLLECTION_NAME;
    
    private int tempCount = 0;
    
    // default zkServerPort - 9181
    public SolrProxy(String zkServerIP, int zkServerPort, String collectionName) {
     // Add your Zookeeper URL
        ZOOKEEPERS = zkServerIP + ":" + Integer.toString(zkServerPort) + "/solr";
        log.info("ZOOKEEPERS        : " + ZOOKEEPERS);
        // Add your Collectin Name
        COLLECTION_NAME = collectionName;
        log.info("COLLECTION_NAME   : " + COLLECTION_NAME);
        
        connectToSolrServer();
    }

    private void connectToSolrServer()
    {
        solrServer = new CloudSolrServer(ZOOKEEPERS);
        solrServer.setDefaultCollection(COLLECTION_NAME);
        solrServer.connect();
    }

    public void uploadDocument(SolrInputDocument solrDocument) throws SolrServerException, IOException
    {        
        solrServer.add(solrDocument);
        tempCount++;
        
        if (tempCount == 1000) {
            if (commit()) {
                tempCount = 0;
                log.debug("committed docs");
            } else {
                log.debug("committed docs failed");
            }
            
        }
    }
    
    public boolean commit() {
        boolean retVal = true;
        
        try {
            solrServer.commit();
        } catch (SolrServerException | IOException e) {
            String header = "caught and handled the solrServer.commit() error by returning retVal as false";
            String formattedException = LogFormatter.formatException(header, "", e);
            exLogger.warn(formattedException);
            
            retVal = false;
        } 
        
        return retVal;
    }
    
    public void shutdown() {
        solrServer.shutdown();
    }
}
