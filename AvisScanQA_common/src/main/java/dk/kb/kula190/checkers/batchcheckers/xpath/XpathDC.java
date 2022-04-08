package dk.kb.kula190.checkers.batchcheckers.xpath;

import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Node;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class XpathDC {
    private Set<String> titles;
    private String date;
    private Set<String> types;
    private Set<String> identifiers;
    private Set<String> formats;
    private Set<String> language;
    private Set<String> coverages;
    
    
    public XpathDC data(DecoratedAttributeParsingEvent decoratedEvent,
                          Node metadataDC,
                          String avis,
                          String roundTrip,
                          LocalDate startDate,
                          LocalDate endDate) {

        //TODO all the other DC terms from http://www.openarchives.org/OAI/2.0/oai_dc.xsd
        XPathSelector xpath = XpathUtils.createXPathSelector("dc", "http://purl.org/dc/elements/1.1/",
                                                             "oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        
        titles = new HashSet<>(xpath.selectStringList(metadataDC, "/oai_dc:dc/dc:title/text()"));
        
        date = xpath.selectString(metadataDC, "/oai_dc:dc/dc:date");
        
        types = new HashSet<>(xpath.selectStringList(metadataDC, "/oai_dc:dc/dc:type/text()"));
        
        identifiers = new HashSet<>(xpath.selectStringList(metadataDC, "/oai_dc:dc/dc:identifier/text()"));
        
        formats = new HashSet<>(xpath.selectStringList(metadataDC, "/oai_dc:dc/dc:format/text()"));
        
        language = new HashSet<>(xpath.selectStringList(metadataDC, "/oai_dc:dc/dc:language/text()"));
    
        coverages = new HashSet<>(xpath.selectStringList(metadataDC, "/oai_dc:dc/dc:coverage/text()"));
        return this;
    }
    /*
    <oai_dc:dc xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
                           xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
        <dc:title>Modersmaalet (Haderslev)</dc:title>
        <dc:title>Modersmaalet (Haderslev) (1901-1938)</dc:title>
        <dc:date>1901-02-11-1938-06-14</dc:date>
        <dc:type>newspaper</dc:type>
        <dc:type>text</dc:type>
        <dc:identifier>dda: 100-9</dc:identifier>
        <dc:identifier>title: modersmaalet_1</dc:identifier>
        <dc:identifier>title_family: modersmaalet</dc:identifier>
        <dc:format>text</dc:format>
        <dc:format>image/tif</dc:format>
        <dc:format>electronic</dc:format>
        <dc:language>dan</dc:language>
        <dc:coverage>Danmark--Sydjylland--Haderslev</dc:coverage>
        <dc:coverage>1901-02-11-1938-06-14</dc:coverage>
    </oai_dc:dc>
     */
    
    public Set<String> getTitles() {
        return titles;
    }
    
    public String getDate() {
        return date;
    }
    
    public Set<String> getTypes() {
        return types;
    }
    
    public Set<String> getIdentifiers() {
        return identifiers;
    }
    
    public Set<String> getFormats() {
        return formats;
    }
    
    public Set<String> getLanguage() {
        return language;
    }
    
    public Set<String> getCoverages() {
        return coverages;
    }
}
