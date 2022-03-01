package dk.kb.kula190.checkers.crosscheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.checkers.singlecheckers.MetsSplitter;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedNodeParsingEvent;
import dk.kb.util.xml.XML;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class MetsChecker extends DecoratedEventHandler {
    
    
    public MetsChecker(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    @Override
    public void injectedFile(DecoratedAttributeParsingEvent decoratedEvent,
                             String injectedType,
                             String avis,
                             LocalDate editionDate,
                             String udgave,
                             String sectionName,
                             Integer pageNumber) throws IOException {
        
        if (MetsSplitter.INJECTED_TYPE_METS.equals(injectedType)) {
            handleMETS(decoratedEvent,
                       decoratedEvent.getAvis(),
                       decoratedEvent.getRoundTrip(),
                       decoratedEvent.getStartDate(),
                       decoratedEvent.getEndDate());
        }
    }
    
    @Override
    public void modsFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {
        
        //        TODO check that this mods is in agreement with the mods from METS
    
        Document document = EventHandlerUtils.handleDocument(event);
        XPathSelector xpath = XpathUtils.createXPathSelector("mods", "http://www.loc.gov/mods/v3");
    
        String digitalOrigin = xpath.selectString(document, "/mods:mods/mods:physicalDescription/mods:digitalOrigin");
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods digital origin should have been {expected} but was {actual}",
                    digitalOrigin,
                    "digitized newspaper"
                   );
    
        Set<String> internetMediaType = new HashSet<>(xpath.selectStringList(document,
                                                                             "/mods:mods/mods:physicalDescription/mods:internetMediaType/text()"));
        Set<String> expectedInternetMediaTypes = Set.of("text", "image/tif");
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods internet media type should have been {expected} but was {actual}",
                    internetMediaType,
                    expectedInternetMediaTypes
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
    
    private void handleMETS(DecoratedAttributeParsingEvent decoratedEvent,
                            String avis,
                            String roundTrip,
                            LocalDate startDate,
                            LocalDate endDate) throws IOException {
        log.debug("injected METS event for {},{},{},{}", avis, roundTrip, startDate, endDate);
        
        XPathSelector xpath = XpathUtils.createXPathSelector("mets", "http://www.loc.gov/METS/",
                                                             "dc", "http://purl.org/dc/elements/1.1/",
                                                             "mix", "http://purl.org/dc/elements/1.1/mix",
                                                             "premis", "http://purl.org/dc/elements/1.1/premis",
                                                             "xlink", "http://www.w3.org/1999/xlink");
        try (InputStream data = decoratedEvent.getData()) {
            Document metsDoc = XML.fromXML(data, true);
            
            //This is the  METS file, without the humongous ADMSEC part
            String x = XML.domToString(metsDoc);
            
            //            TODO check that DC, MARC and MODS are in agreement
            
            Node metadataMods = xpath.selectNode(metsDoc,
                                                 "/mets:mets/mets:dmdSec[@ID='DMD1']/mets:mdWrap/mets:xmlData/*");
            //TODO check this with same checks as in ModsChecker
            
            
            Node metadataDC = xpath.selectNode(metsDoc,
                                               "/mets:mets/mets:dmdSec[@ID='DMD2']/mets:mdWrap/mets:xmlData/*");
            //            log.debug("DC metadata\n{}",XML.domToString(metadataDC));
            
            Node metadataMarc = xpath.selectNode(metsDoc,
                                                 "/mets:mets/mets:dmdSec[@ID='DMD3']/mets:mdWrap/mets:xmlData/*");
            
            
            // TODO extract file list from fileSec for checks below
            
        } catch (ParserConfigurationException | SAXException | TransformerException e) {
            throw new IOException("Failed to parse METS data from " + decoratedEvent.getLocation(), e);
        }
    }
    
    @Override
    public void tiffFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        //TODO register that this tiff file have been found
    }
    
    @Override
    public void altoFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        //TODO register that this alto file have been found
    }
    
    @Override
    public void batchEnds(DecoratedNodeParsingEvent event,
                          String avis,
                          String roundTrip,
                          LocalDate startDate,
                          LocalDate endDate) throws IOException {
        //TODO check that all found tif files are in METS fileSec
        //TODO check that all files in fileSec have been found
    }
    
}
