package dk.kb.kula190.checkers.crosscheckers;

import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Document;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class XpathMods {
    private String digitalOrigin;
    private HashSet<String> mediaType;
    private String physicalDescription;
    private String dateIssuedStart;
    private String dateIssuedEnd;
    private String temporalStart;
    private String temporalEnd;
    private String fileFamily;

    public XpathMods() {

    }

    public void setModsXpathData(DecoratedAttributeParsingEvent event,
                                 String avis,
                                 String roundTrip,
                                 LocalDate startDate,
                                 LocalDate endDate) throws IOException {

        Document document = EventHandlerUtils.handleDocument(event);
        XPathSelector xpath = XpathUtils.createXPathSelector("mods", "http://www.loc.gov/mods/v3");
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
