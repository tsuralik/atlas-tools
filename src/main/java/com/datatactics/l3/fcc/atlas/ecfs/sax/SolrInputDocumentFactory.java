package com.datatactics.l3.fcc.atlas.ecfs.sax;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.jdom2.Element;

public class SolrInputDocumentFactory {
    
    private String id;
    private String applicant;
    private String applicant_sort;
    private String brief;
    private String city;
    private String dateRcpt;
    private String disseminated;
    private String exParte;
    private String modified;
    private String pages;
    private String proceeding;
    private String regFlexAnalysis;
    private String smallBusinessImpact;
    private String stateCd;
    private String submissionType;
    private String text;
    private String viewingStatus;
    private String zip;
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
                city = text("str", arrElement);
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
                stateCd = text("str", arrElement);
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
                zip = text("str", arrElement);
            }
        }
    }
    
    private String text(String childName, Element element) {
        String text = element.getChild(childName).getText();
        return text;
    }
    
    private boolean nameMatch(String name, Element element) {
        return name.equalsIgnoreCase(element.getAttribute("name").getValue());
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
}