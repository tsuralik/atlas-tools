package com.datatactics.l3.fcc.utils;

import java.io.File;
import java.io.PrintWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.datatactics.l3.fcc.atlas.ecfs.sax.SolrInputDocumentFactory;

public class XmlSummarizer {
    private static Logger logger = LogManager.getLogger(XmlSummarizer.class);

    private File streamFile;
    private PrintWriter printWriter;
    private XMLStreamWriter streamWriter;
    private String currentWorkingDirectory;
    private String logDirectory;
    private long index = 0;
    
    public XmlSummarizer() {
        currentWorkingDirectory = System.getProperties().getProperty("user.dir");
        // are these conditions even possible??
        if ((currentWorkingDirectory != null) && !currentWorkingDirectory.trim().equals("")) {
            logDirectory = currentWorkingDirectory + File.separator + "logs";
        } else {
            logDirectory = ".";
        }
        
        File logDirectoryFile = new File(logDirectory);
        if (!logDirectoryFile.exists()) {
            logDirectoryFile.mkdir();
        }
    }

    public void openStreamWriter() {
        try {
            streamFile = new File(logDirectory, "exceptions-ingest.xml");
            printWriter = new PrintWriter(streamFile);
            streamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(printWriter);
            streamWriter.writeStartDocument();
            
            streamWriter.flush();
            printWriter.println();
            streamWriter.writeStartElement("FileIngest");
            streamWriter.flush();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void startFileSummary(File ingestingFile) throws XMLStreamException {
        streamWriter.writeStartElement("File");        
        streamWriter.writeAttribute("name", ingestingFile.getName());
        streamWriter.writeAttribute("path", ingestingFile.getPath());    
        streamWriter.flush();
    }
    
    /**
     * Writes a <code>Failure</code> tag for each exception.
     * @param index
     * @param ingestingFile
     * @param solrDoc
     * @param ex
     * @throws XMLStreamException
     */
    public void summarizeException(File ingestingFile, SolrInputDocumentFactory solrDoc, Exception ex) throws XMLStreamException {
        logger.debug("summarizeException");
        
        String id = (solrDoc == null ? "-" : solrDoc.getId());

        streamWriter.flush();
        printWriter.println();
        streamWriter.writeStartElement("Failure");
        streamWriter.writeAttribute("index", Long.toString(++index));
        streamWriter.writeAttribute("id", id);
        streamWriter.writeAttribute("textLength", Integer.toString(solrDoc.getTextLength()));        
        streamWriter.writeAttribute("exception", ex.getLocalizedMessage());
        streamWriter.writeEndElement();  //  Failure
        streamWriter.flush();
    }
    
    public void endFileSummary(int successCount, int failedCount) throws XMLStreamException {
        streamWriter.flush();
        printWriter.println();
        streamWriter.writeStartElement("Summary");
        streamWriter.writeAttribute("Success", Integer.toString(successCount));
        streamWriter.writeAttribute("Failed", Integer.toString(failedCount));
        streamWriter.writeEndElement();  // Summary
        streamWriter.flush();
        printWriter.println();
        streamWriter.writeEndElement();  // File
        streamWriter.flush();
    }
    
    public void closeStreamWriter(int successCount, int failedCount) throws XMLStreamException {
        streamWriter.flush();
        printWriter.println();
        streamWriter.writeStartElement("OverallSummary");
        streamWriter.writeAttribute("Success", Integer.toString(successCount));
        streamWriter.writeAttribute("Failed", Integer.toString(failedCount));
        streamWriter.writeEndElement();  // Summary

        streamWriter.flush();
        printWriter.println();        
        streamWriter.writeEndElement();  // FileIngest
        streamWriter.writeEndDocument();  
            
        streamWriter.flush();
        streamWriter.close();
    }
}
