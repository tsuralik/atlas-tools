package com.datatactics.l3.fcc.atlas.ecfs.sax;

import java.io.File;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import com.datatactics.l3.fcc.atlas.solr.FileIngestException;
import com.datatactics.l3.fcc.atlas.solr.SolrProxy;
import com.datatactics.l3.fcc.utils.LogFormatter;
import com.datatactics.l3.fcc.utils.XmlSummarizer;

public class XmlSaxParser {
    private static Logger logger = LogManager.getLogger(XmlSaxParser.class);
    private static Logger exLogger = LogManager.getLogger("Exceptions");
    private static Logger statusLogger = LogManager.getLogger("RunningStatus");
    
    private final static int STATUS_UPDATE_COUNT = 1000;

    private XmlSummarizer xmlSummarizer = null;
    
    private int overallSuccessCount = 0;
    private int overallFailedCount = 0;
    
    private int currentSuccessCount = 0;
    private int currentFailedCount = 0;
    
    public XmlSaxParser(XmlSummarizer xmlSummarizer) {
        this.xmlSummarizer = xmlSummarizer;
        this.xmlSummarizer.openStreamWriter();
    }

    public void parseFile(SolrProxy solr, File fileToIngest) throws FileIngestException {
        logger.debug("parse file: " + fileToIngest.getAbsolutePath());

        try {
            // reset these values when parsing the next file
            currentSuccessCount = 0;
            currentFailedCount = 0;
            
            xmlSummarizer.startFileSummary(fileToIngest);
            
            SAXBuilder builder = new SAXBuilder();
            
            Document xmldoc = null;
            try {
                xmldoc = (Document) builder.build(fileToIngest);
            }
            catch(Exception e) {
                String header = "caught and handled parseFile exception building xml document";
                String formattedException = LogFormatter.formatException(header, "", e);
                exLogger.warn(formattedException);
                
                throw new FileIngestException(fileToIngest, e);
            }

            Element rootNode = xmldoc.getRootElement().getChild("result");            
            List<Element> docs = rootNode.getChildren("doc");

            for (int i = 0; i < docs.size(); i++) {
               Element docElement = (Element) docs.get(i);
               SolrInputDocumentFactory ecfsDoc = null;
               boolean uploaded = false;
               
               try {
                   ecfsDoc = new SolrInputDocumentFactory(docElement);
               } catch(Exception e) {
                   String header = "caught and handled the following error by ignoring the docElement";
                   XMLOutputter outputter = new XMLOutputter();
                   String footer = outputter.outputString(docElement);
                   String formattedException = LogFormatter.formatException(header, footer, e);
                   exLogger.warn(formattedException);
                   try {
                       xmlSummarizer.summarizeException(fileToIngest, null, e);
                   } catch (XMLStreamException xmlse) {
                       header = "caught and handled the following error by logging it to the exceptions log";
                       outputter = new XMLOutputter();
                       footer = outputter.outputString(docElement);
                       formattedException = LogFormatter.formatException(header, footer, xmlse);
                       exLogger.warn(formattedException);
                   }
               }

               if (ecfsDoc != null) {
                   try {
                       ecfsDoc.parse();
                       if (ecfsDoc.hasDefaultZip()) {
                           logger.debug("solrInputDoc has default zip    : " + ecfsDoc.getId());
                       }
                       if (ecfsDoc.hasDefaultStateCd()) {
                           logger.debug("solrInputDoc has default stateCd: " + ecfsDoc.getId());
                       }
                       solr.uploadDocument(ecfsDoc.createSolrInputDocument()); 
                       uploaded = true;
                   } catch(Exception e) {
                       String headerFormat = "caught and handled the following error by not uploading the solrDocument with id [%1$s] from [%2$s]";
                       String header = String.format(headerFormat, ecfsDoc.getId(), fileToIngest.getAbsolutePath());
                       XMLOutputter outputter = new XMLOutputter();
                       String footer = "Xml docElement was:\n" + outputter.outputString(docElement);
                       String formattedException = LogFormatter.formatException(header, footer, e);
                       exLogger.warn(formattedException);
                       
                       try {
                           xmlSummarizer.summarizeException(fileToIngest, ecfsDoc, e);
                       } catch (XMLStreamException xmlse) {
                           header = "caught and handled the following error by not logging it to the summarizer file";
                           outputter = new XMLOutputter();
                           footer = outputter.outputString(docElement);
                           formattedException = LogFormatter.formatException(header, footer, xmlse);
                           exLogger.warn(formattedException);
                       }
                   }
               }
               
               if (uploaded) {                   
                   currentSuccessCount++;
               } else {
                   currentFailedCount++;
               }

               if (numOfCurrentDocuments() % STATUS_UPDATE_COUNT == 0) {
                   String stringFormat = "%1$8d documents and counting from %2$s";
                   statusLogger.trace(String.format(stringFormat, numOfCurrentDocuments(), fileToIngest.getAbsolutePath()));
               }
            }
        } catch (Exception e) {
            String header = "caught and handled parseFile exception by exiting the parsing operation";
            String formattedException = LogFormatter.formatException(header, "", e);
            exLogger.warn(formattedException);
            
            throw new FileIngestException(fileToIngest, e);
        } finally { 
            try {
                overallSuccessCount += numOfCurrentFileSuccessCount();
                overallFailedCount += numOfCurrentFileFailedCount();
                xmlSummarizer.endFileSummary(numOfCurrentFileSuccessCount(), numOfCurrentFileFailedCount());
            } catch (XMLStreamException xmlse) {
                throw new FileIngestException(xmlse);
            }
        }
    }
    
    public int numOfOverallDocuments() {
        return numOfOverallSuccessDocuments() + numOfOverallFailedDocuments();
    }
    
    public int numOfOverallSuccessDocuments() {
        return overallSuccessCount;
    }
    
    public int numOfOverallFailedDocuments() {
        return overallFailedCount;
    }
    
    public boolean hadSuccessDocument() {
        return numOfOverallSuccessDocuments() > 0;
    }
    
    public boolean hadFailedDocument() {
        return numOfOverallFailedDocuments() > 0;
    }
    
    public int numOfCurrentDocuments() {
        return numOfCurrentFileSuccessCount() + numOfCurrentFileFailedCount();
    }
    
    public int numOfCurrentFileSuccessCount() {
        return currentSuccessCount;
    }
    
    public int numOfCurrentFileFailedCount() {
        return currentFailedCount;
        
    }
}
