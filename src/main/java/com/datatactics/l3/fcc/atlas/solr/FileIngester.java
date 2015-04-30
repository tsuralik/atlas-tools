package com.datatactics.l3.fcc.atlas.solr;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;

import com.datatactics.l3.fcc.atlas.ecfs.sax.XmlSaxParser;
import com.datatactics.l3.fcc.utils.LogFormatter;
import com.datatactics.l3.fcc.utils.XmlSummarizer;


public class FileIngester {
    private static Logger log = LogManager.getLogger(FileIngester.class);
    private static Logger exLogger = LogManager.getLogger("Exceptions");
    
    //private int docsSucceededCount = 0;
    //private int docsFailedCount = 0;
    private XmlSummarizer xmlSummarizer = null;
    private XmlSaxParser xmlSaxParser = null;
    private SolrProxy solr = null;
    
    public FileIngester(String zkServerIP, int zkServerPort, String collectionName, String filePath, boolean doRecurse) {

        log.debug("starting FileIngester");
        log.debug("max memory: " + Runtime.getRuntime().maxMemory());
        
        try {
            initialize(zkServerIP, zkServerPort, collectionName);
            File file = new File(filePath);
            
            if (doRecurse) {
                recursiveRead(solr, file);
            } else {
                readFile(solr, file);
            }
        } catch (Exception e) {
            String header = "caught and ignoring unknown error";
            String formattedException = LogFormatter.formatException(header, "", e);
            exLogger.warn(formattedException);
        } finally {
            int successCount = xmlSaxParser.numOfOverallSuccessDocuments();
            int failedCount = xmlSaxParser.numOfOverallFailedDocuments();
            
            try {
                xmlSummarizer.closeStreamWriter(successCount, failedCount);
            } catch (XMLStreamException xmlse) {
                String header = "caught and ignoring error closing xmlSummarizer stream writer";
                String formattedException = LogFormatter.formatException(header, "", xmlse);
                exLogger.warn(formattedException);
            }

            String summary = String.format("Succeeded: %1$8d | failed: %2$8d", successCount, failedCount);
            log.info(summary);

            log.debug("exiting FileIngester");
            solr.shutdown();
            System.exit(0);
        }
    }
    
    private void initialize(String zkServerIP, int zkServerPort, String collectionName) {
        xmlSummarizer = new XmlSummarizer();
        xmlSaxParser = new XmlSaxParser(xmlSummarizer);        
        solr = new SolrProxy(zkServerIP, zkServerPort, collectionName);
    }
    
    private int readFile(SolrProxy solr, File file) {
        log.debug("read file: " + file.getAbsolutePath());
        int retVal = -1;
        
        if (file.exists() && file.isFile()) {
            try {
                parseFile(solr, file);
            } catch (FileIngestException e) {
                String header = "caught and handled the following error ignoring the file";
                String formattedException = LogFormatter.formatException(header, "", e);
                exLogger.warn(formattedException);
            }    
        } else {
            log.warn(String.format("filePath is NOT a file - it is a directory : %1$s", file.getAbsolutePath()));
        }
        
        return retVal;
    }
    
    private void recursiveRead(SolrProxy solr, File file) {
        log.debug("read path: " + file.getAbsolutePath());
       
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File listedFile: files) {
                    recursiveRead(solr, listedFile);
                }
            } else { // just a file
                readFile(solr, file);
            }
        }
    }
    
    private void parseFile(SolrProxy solr, File fileToIngest) throws FileIngestException {
        log.debug("parse file: " + fileToIngest.getAbsolutePath());
        
        long timeStart;
        long timeEnd;

        timeStart = Calendar.getInstance().getTimeInMillis();
        try {
            xmlSaxParser.parseFile(solr, fileToIngest);
        } catch (FileIngestException fie) {
            String header = "could not close out parse file: " + fileToIngest;
            String formattedException = LogFormatter.formatException(header, "", fie);
            exLogger.warn(formattedException);
        }
        timeEnd = Calendar.getInstance().getTimeInMillis();
        
        if (xmlSaxParser.hadSuccessDocument()) {
            solr.commit();
        }

        catalogDuration(fileToIngest, xmlSaxParser, timeStart, timeEnd);
    }
    
    private String formatTime(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long secondsInTheMinutes = TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
        long remainingSeconds = totalSeconds - secondsInTheMinutes;
        long milliseconds = TimeUnit.MILLISECONDS.toMillis(millis);
        
        return String.format("%1$02d:%2$02d.%3$04d", minutes, remainingSeconds, milliseconds);
    }
    
    private void catalogDuration(File fileToIngest, XmlSaxParser parser, long timeStart, long timeEnd) {
        long timeDiff = timeEnd - timeStart;
        String timeFormat = "Doc success [%1$8d] & failed [%2$8s] took [%3$s] from file [%4$s]";
        log.info(String.format(timeFormat, 
                parser.numOfCurrentFileSuccessCount(), 
                parser.numOfCurrentFileFailedCount(), 
                formatTime(timeDiff), 
                fileToIngest.getAbsolutePath()));
    }
    
    /**********************************************************************/
    /************************* APPLICATION MAIN ***************************/
    /**********************************************************************/

    public static void main(String[] args) throws JDOMException, IOException {
        if (args.length < 5) {
            log.warn("need five command line arguments - zookeeper IP, zookeeper port, the name of the collection, a full file path for ingest files, and a recursive boolean (true or false)");
            System.exit(0);
        }
        
        String ip = args[0];
        int port = Integer.parseInt(args[1]);
        String collectionName = args[2];
        String filePath = args[3];
        // non-boolean string values will equate to false (see Boolean javadoc)
        boolean recurse = Boolean.parseBoolean(args[4]);

        new FileIngester(ip, port, collectionName, filePath, recurse);
    }
}
