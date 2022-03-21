package dk.kb.kula190.checkers.pagecheckers.xpath;

import dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Document;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;

import static dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils.lastName;
import static org.apache.commons.io.FilenameUtils.removeExtension;

public class XpathMetsMods {
    private String digitalOrigin;
    private HashSet<String> mediaType;
    private String physicalDescription;
    private String dateIssuedStart;
    private String dateIssuedEnd;
    private String temporalStart;
    private String temporalEnd;
    private String fileFamily;

    public XpathMetsMods(){

    }
    public void setMetsModsInjectedFileData(DecoratedAttributeParsingEvent decoratedEvent,
                                           String injectedType,
                                           String avis,
                                           LocalDate editionDate,
                                           String udgave,
                                           String sectionName,
                                           Integer pageNumber) throws IOException {
        Document document = EventHandlerUtils.handleDocument(decoratedEvent);
        XPathSelector xpath = XpathUtils.createXPathSelector("mix", "http://www.loc.gov/mix/v20");

        digitalOrigin = xpath.selectString(document, "/mods:mods/mods:physicalDescription/mods:digitalOrigin");
        mediaType = new HashSet<>(xpath.selectStringList(document,
                                                         "/mods:mods/mods:physicalDescription/mods:internetMediaType" +
                                                         "/text()"));
        physicalDescription = xpath.selectString(document, "/mods:mods/mods:physicalDescription/mods:form/text()");
        dateIssuedStart = xpath.selectString(document,
                                             "/mods:mods/mods:originInfo/mods:dateIssued[@point='start']");
        temporalStart = xpath.selectString(document, "/mods:mods/mods:subject/mods:temporal[@point='start']");
        dateIssuedEnd = xpath.selectString(document, "/mods:mods/mods:originInfo/mods:dateIssued[@point='end']");
        temporalEnd = xpath.selectString(document, "/mods:mods/mods:subject/mods:temporal[@point='end']");
        fileFamily = xpath.selectString(document, "/mods:mods/mods:identifier[@type='title_family']");

    }
    public String getDigitalOrigin() {
        return digitalOrigin;
    }
    public HashSet<String> getMediaType() {
        return mediaType;
    }

    public String getPhysicalDescription() {
        return physicalDescription;
    }

    public String getDateIssuedStart() {
        return dateIssuedStart;
    }

    public String getDateIssuedEnd() {
        return dateIssuedEnd;
    }

    public String getTemporalStart() {
        return temporalStart;
    }

    public String getTemporalEnd() {
        return temporalEnd;
    }

    public String getFileFamily() {
        return fileFamily;
    }

}

