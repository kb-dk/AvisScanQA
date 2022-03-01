package dk.kb.kula190.checkers.singlecheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.util.xml.XML;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

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
        String internetMediaType1 = xpath.selectString(document, "/mods:mods/mods:physicalDescription/mods:internetMediaType[1]/text()");
        String internetMediaType2 = xpath.selectString(document, "/mods:mods/mods:physicalDescription/mods:internetMediaType[2]/text()");
        String form = xpath.selectString(document, "/mods:mods/mods:physicalDescription/mods:form/text()");
        String dateIssuedStart = xpath.selectString(document,"/mods:mods/mods:originInfo/mods:dateIssued[1]");
        String temporalStart = xpath.selectString(document,"/mods:mods/mods:subject/mods:temporal[1]");
        String dateIssuedEnd = xpath.selectString(document,"/mods:mods/mods:originInfo/mods:dateIssued[2]");
        String temporalEnd = xpath.selectString(document,"/mods:mods/mods:subject/mods:temporal[2]");
        String titleFamily = xpath.selectString(document, "/mods:mods/mods:identifier[3]");
        String title = event.getName().split("_")[0];

        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods digital origin should have been {expected} but was {actual}",
                    digitalOrigin,
                    "digitized newspaper"
                   );
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods internet media type should have been {expected} but was {actual}",
                    internetMediaType1,
                    "text"
                   );
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods internet media type should have been {expected} but was {actual}",
                    internetMediaType2,
                    "image/tif"
                   );
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods physical description form should have been {expected} but was {actual}",
                    form,
                    "electronic"
                   );
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods start dates do not match date issued: {actual}, temporal: {expected}",
                    dateIssuedStart,
                    temporalStart);
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods end dates do not match date issued: {actual}, temporal: {expected}",
                    dateIssuedEnd,
                    temporalEnd);
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods file family was incorrect should have been {expected} but was {actual}",
                    titleFamily,
                    title);

    }

}
