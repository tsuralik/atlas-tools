package com.datatactics.l3.fcc.atlas.ecfs.sax;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.jdom2.Attribute;
import org.jdom2.Element;

public class SolrInputDocumentFactory {
    private static Logger log = LogManager.getLogger(SolrInputDocumentFactory.class);

    private final static String DEFAULT_CITY    = "N/A";
    private final static String DEFAULT_STATE   = "ZZ";
    private final static String DEFAULT_ZIP     = "00000";
    
    private String id;
    private String applicant;
    private String applicant_sort;
    private String brief;
    private String city = DEFAULT_CITY;
    private String dateRcpt;
    private String disseminated;
    private String exParte;
    private String modified;
    private String pages;
    private String proceeding;
    private String regFlexAnalysis;
    private String smallBusinessImpact;
    private String stateCd = DEFAULT_STATE;
    private String submissionType;
    private String text;
    private String viewingStatus;
    private String zip = DEFAULT_ZIP;
    private float  score;
    
    private Element docElement;
    
    public SolrInputDocumentFactory(Element docElement) {
        this.docElement = docElement;

        // Read JUST the ID for now
        List<Element> arrs = docElement.getChildren("arr");
        for(int j =0 ; j <arrs.size(); j++)
        {
            Element arrElement = (Element) arrs.get(j);
            if (nameMatch("id", arrElement)) {
                id = text("long", arrElement);
            }            
        }
    }
    
    public String getId() {
        return id;
    }
    
    public int getTextLength() {
        return (text == null ? -1 : text.length());
    }
    
    public void parse() {
        Element floatElement = docElement.getChild("float");
        score = (floatElement == null ? -1 : Float.parseFloat(floatElement.getText()));
        
        List<Element> arrs = docElement.getChildren("arr");
        for(int j =0 ; j <arrs.size(); j++)
        {
            Element arrElement = (Element) arrs.get(j);
            if (nameMatch("applicant", arrElement)) {
                applicant = text("str", arrElement);
            }
            else if (nameMatch("applicant_sort", arrElement)) {
                applicant_sort = text("str", arrElement);
            }
            else if (nameMatch("brief", arrElement)) {
                brief = text("bool", arrElement);
            }
            else if (nameMatch("city", arrElement)) {
                city = text("str", arrElement, "city", DEFAULT_CITY);
            }
            else if (nameMatch("dateRcpt", arrElement)) {
                dateRcpt = text("date", arrElement);
            }
            else if (nameMatch("disseminated", arrElement)) {
                disseminated = text("date", arrElement);
            }
            else if (nameMatch("exParte", arrElement)) {
                exParte = text("bool", arrElement);
            }
            else if (nameMatch("modified", arrElement)) {
                modified = text("date", arrElement);
            }
            else if (nameMatch("pages", arrElement)) {
                pages = text("int", arrElement);
            }
            else if (nameMatch("proceeding", arrElement)) {
                proceeding = text("str", arrElement);
            }
            else if (nameMatch("regFlexAnalysis", arrElement)) {
                regFlexAnalysis = text("bool", arrElement);
            }
            else if (nameMatch("smallBusinessImpact", arrElement)) {
                smallBusinessImpact = text("bool", arrElement);
            }
            else if (nameMatch("stateCd", arrElement)) {
                stateCd = text("str", arrElement, "stateCd", DEFAULT_STATE);
            }
            else if (nameMatch("submissionType", arrElement)) {
                submissionType = text("str", arrElement);
            }
            else if (nameMatch("text", arrElement)) {
                text = text("str", arrElement);
            }
            else if (nameMatch("viewingStatus", arrElement)) {
                viewingStatus = text("str", arrElement);
            }
            else if (nameMatch("zip", arrElement)) {
                zip = text("str", arrElement, "zip", DEFAULT_ZIP);
            }
        }
    }
    
    private String text(String childName, Element element) {
        String retVal = "";
        Element childElement = element.getChild(childName);
        if (childElement != null) {
            retVal = childElement.getText().trim();
        }
        return retVal;
    }
    
    private String text(String childName, Element element, String variableName, String defaultValue) {
        String value = text(childName, element);
        if (value.isEmpty()) {
            log.debug("use default: " + variableName);
            value = defaultValue;
        } 
        return value;
    }
    
    private boolean nameMatch(String name, Element element) {
        boolean retVal = false;
        Attribute attr = element.getAttribute("name");
        if (attr != null) {
            String value = attr.getValue();
            retVal = (value != null && value.equalsIgnoreCase(name));
        }
        return retVal;
    }
    
    public SolrInputDocument createSolrInputDocument() {

        SolrInputDocument document = new SolrInputDocument();
        
        document.addField("id", id);
        document.addField("applicant", applicant);
        document.addField("applicant_sort", applicant_sort);
        document.addField("brief", brief);
        document.addField("city", city);
        document.addField("dateRcpt", dateRcpt);
        document.addField("disseminated", disseminated);
        document.addField("exParte", exParte);
        document.addField("modified", modified);
        document.addField("pages", pages);
        document.addField("proceeding", proceeding);
        document.addField("regFlexAnalysis", regFlexAnalysis);
        document.addField("scorex", score);
        document.addField("smallBusinessImpact", smallBusinessImpact);
        document.addField("stateCd", stateCd);
        document.addField("submissionType", submissionType);
        document.addField("text", text);
        document.addField("viewingStatus", viewingStatus);
        document.addField("zip", zip);
        document.addField("deleted", false);
        
        return document;
    }
    
    public boolean hasDefaultZip() {
        return zip.equals(DEFAULT_ZIP);
    }
    
    public boolean hasDefaultStateCd() {
        return stateCd.equals(DEFAULT_STATE);  
    }
    
    public boolean hasDefaultCity() {
        return city.equals(DEFAULT_CITY);
    }
}