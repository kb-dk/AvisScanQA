package dk.kb.kula190.checkers.singlecheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Document;

import java.io.IOException;
import java.time.LocalDate;

public class ModsChecker extends DecoratedEventHandler {
    public ModsChecker(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    
    @Override
    public void modsFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {
        Document document = EventHandlerUtils.handleDocument(event);
        XPathSelector xpath = XpathUtils.createXPathSelector("mods", "http://www.loc.gov/mods/v3");
        
        String digitalOrigin = xpath.selectString(document, "/mods:mods/mods:physicalDescription/mods:digitalOrigin");
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods digital origin should have been {expected} but was {actual}",
                    digitalOrigin,
                    "digitized newspaper"
                   );
        
        String internetMediaType1 = xpath.selectString(document,
                                                       "/mods:mods/mods:physicalDescription/mods:internetMediaType[1]/text()");
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods internet media type should have been {expected} but was {actual}",
                    internetMediaType1,
                    "text"
                   );
        
        String internetMediaType2 = xpath.selectString(document,
                                                       "/mods:mods/mods:physicalDescription/mods:internetMediaType[2]/text()");
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods internet media type should have been {expected} but was {actual}",
                    internetMediaType2,
                    "image/tif"
                   );
        
        String form = xpath.selectString(document, "/mods:mods/mods:physicalDescription/mods:form/text()");
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods physical description form should have been {expected} but was {actual}",
                    form,
                    "electronic"
                   );
        
        
        String dateIssuedStart = xpath.selectString(document,
                                                    "/mods:mods/mods:originInfo/mods:dateIssued[@point='start']");
        String temporalStart = xpath.selectString(document, "/mods:mods/mods:subject/mods:temporal[@point='start']");
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods start dates do not match date issued: {actual}, temporal: {expected}",
                    dateIssuedStart,
                    temporalStart);
        
        
        String dateIssuedEnd = xpath.selectString(document, "/mods:mods/mods:originInfo/mods:dateIssued[@point='end']");
        String temporalEnd = xpath.selectString(document, "/mods:mods/mods:subject/mods:temporal[@point='end']");
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods end dates do not match date issued: {actual}, temporal: {expected}",
                    dateIssuedEnd,
                    temporalEnd);
        
        String titleFamily = xpath.selectString(document, "/mods:mods/mods:identifier[@type='title_family']");
        String title = event.getName().split("_")[0];
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods file family was incorrect should have been {expected} but was {actual}",
                    titleFamily,
                    title);
    }
}
