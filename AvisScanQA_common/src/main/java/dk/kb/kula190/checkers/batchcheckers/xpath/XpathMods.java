package dk.kb.kula190.checkers.batchcheckers.xpath;

import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Node;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class XpathMods {
    
    private HashSet<String> titles;
    
    private String originPlace;
    private String originDayIssuedStart;
    private String originDayIssuedEnd;
    private String originIssuance;
    
    private String genre;
    private String typeOfResource;
    private Set<String> identifiers;
    
    //physicalDescription
    private String digitalOrigin;
    private Set<String> mimetypes;
    private String form;
    
    private String language;
    
    
    private String temporalStart;
    
    private String temporalEnd;
    
    private String catalogLanguage;
    private String subjectCountry;
    private String subjectArea;
    private String subjectCity;
    private String subjectGeographicCode;
    
    
    public XpathMods() {
    
    }
    
    public void setModsDAta(DecoratedAttributeParsingEvent decoratedEvent,
                            Node metadataMods,
                            String avis,
                            String roundTrip,
                            LocalDate startDate,
                            LocalDate endDate) throws IOException {
        XPathSelector xpath = XpathUtils.createXPathSelector("mods", "http://www.loc.gov/mods/v3");
        
        
        titles = new HashSet<>(xpath.selectStringList(metadataMods, "/mods:mods/mods:titleInfo/mods:title/text()"));
        
        
        originPlace          = xpath.selectString(metadataMods,
                                                  "/mods:mods/mods:originInfo/mods:place/mods:placeTerm");
        originDayIssuedStart = xpath.selectString(metadataMods,
                                                  "/mods:mods/mods:originInfo/mods:dateIssued[@point='start']");
        originDayIssuedEnd   = xpath.selectString(metadataMods,
                                                  "/mods:mods/mods:originInfo/mods:dateIssued[@point='end']");
        
        originIssuance = xpath.selectString(metadataMods, "/mods:mods/mods:originInfo/mods:issuance");
        
        
        genre = xpath.selectString(metadataMods, "/mods:mods/mods:genre");
        
        typeOfResource = xpath.selectString(metadataMods,
                                            "/mods:mods/mods:typeOfResource");
        
        identifiers = new HashSet<>(xpath.selectStringList(metadataMods, "/mods:mods/mods:identifier/text()"));
        
        
        //mods:physicalDescription
        digitalOrigin = xpath.selectString(metadataMods,
                                           "/mods:mods/mods:physicalDescription/mods:digitalOrigin");
        
        mimetypes = new HashSet<>(xpath.selectStringList(metadataMods,
                                                         "/mods:mods/mods:physicalDescription/mods:internetMediaType/text()"));
        
        form = xpath.selectString(metadataMods, "/mods:mods/mods:physicalDescription/mods:form");
        
        
        language = xpath.selectString(metadataMods, "/mods:mods/mods:language/mods:languageTerm");
        
        
        //mods:subject
        temporalStart = xpath.selectString(metadataMods, "/mods:mods/mods:subject/mods:temporal[@point='start']");
        
        
        temporalEnd = xpath.selectString(metadataMods, "/mods:mods/mods:subject/mods:temporal[@point='end']");
    
        subjectCountry = xpath.selectString(metadataMods, "/mods:mods/mods:subject/mods:hierarchicalGeographic/mods:country");
        subjectArea = xpath.selectString(metadataMods, "/mods:mods/mods:subject/mods:hierarchicalGeographic/mods:area");
        subjectCity = xpath.selectString(metadataMods, "/mods:mods/mods:subject/mods:hierarchicalGeographic/mods:city");
        subjectGeographicCode = xpath.selectString(metadataMods, "/mods:mods/mods:subject/mods:geographicCode");
    
    
        //mods:recordInfo
        catalogLanguage = xpath.selectString(metadataMods,
                                             "/mods:mods/mods:recordInfo/mods:languageOfCataloging/mods:languageTerm");
        
        
    }
    
    
    /*
    <mods xmlns="http://www.loc.gov/mods/v3">
        <titleInfo>
            <title>Modersmaalet (Haderslev)</title>
        </titleInfo>
        <titleInfo type="uniform">
            <title>Modersmaalet (Haderslev) (1901-1938)</title>
        </titleInfo>
        <originInfo>
            <place>
                <placeTerm type="text">Haderslev</placeTerm>
            </place>
            <dateIssued encoding="iso8601" keyDate="yes" point="start">1901-02-11</dateIssued>
            <dateIssued encoding="iso8601" point="end">1938-06-14</dateIssued>
            <issuance>serial</issuance>
        </originInfo>
        <genre authority="marcgt">newspaper</genre>
        <typeOfResource>text</typeOfResource>
        <identifier type="dda">100-9</identifier>
        <identifier type="title">modersmaalet_1</identifier>
        <identifier type="title_family">modersmaalet</identifier>
        <physicalDescription>
            <digitalOrigin>digitized newspaper</digitalOrigin>
            <internetMediaType>text</internetMediaType>
            <internetMediaType>image/tif</internetMediaType>
            <form authority="marcform">electronic</form>
        </physicalDescription>
        <language>
            <languageTerm authority="iso639-2b" type="code">dan</languageTerm>
        </language>
        <subject usage="primary">
            <temporal encoding="iso8601" point="start">1901-02-11</temporal>
            <temporal encoding="iso8601" point="end">1938-06-14</temporal>
            <hierarchicalGeographic>
                <country>Danmark</country>
                <area>Sydjylland</area>
                <city>Haderslev</city>
            </hierarchicalGeographic>
            <geographicCode>100</geographicCode>
        </subject>
        <recordInfo>
            <languageOfCataloging>
                <languageTerm authority="iso639-2b" type="code">dan</languageTerm>
            </languageOfCataloging>
            <recordCreationDate encoding="iso8601">2021-03-24</recordCreationDate>
        </recordInfo>
    </mods>
     */
    
    public HashSet<String> getTitles() {
        return titles;
    }
    
    public String getOriginPlace() {
        return originPlace;
    }
    
    public String getOriginDayIssuedStart() {
        return originDayIssuedStart;
    }
    
    public String getOriginDayIssuedEnd() {
        return originDayIssuedEnd;
    }
    
    public String getOriginIssuance() {
        return originIssuance;
    }
    
    public String getGenre() {
        return genre;
    }
    
    public String getTypeOfResource() {
        return typeOfResource;
    }
    
    public Set<String> getIdentifiers() {
        return identifiers;
    }
    
    public String getDigitalOrigin() {
        return digitalOrigin;
    }
    
    public Set<String> getMimetypes() {
        return mimetypes;
    }
    
    public String getForm() {
        return form;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public String getTemporalStart() {
        return temporalStart;
    }
    
    public String getTemporalEnd() {
        return temporalEnd;
    }
    
    public String getSubjectCountry() {
        return subjectCountry;
    }
    
    public String getSubjectArea() {
        return subjectArea;
    }
    
    public String getSubjectCity() {
        return subjectCity;
    }
    
    public String getSubjectGeographicCode() {
        return subjectGeographicCode;
    }
    
    public String getCatalogLanguage() {
        return catalogLanguage;
    }
}

