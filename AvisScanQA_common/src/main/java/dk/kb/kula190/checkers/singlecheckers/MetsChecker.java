package dk.kb.kula190.checkers.singlecheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
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
        
        switch (injectedType) {
            case MetsSplitter.INJECTED_TYPE_METS -> handleMETS(decoratedEvent,
                                                               decoratedEvent.getAvis(),
                                                               decoratedEvent.getRoundTrip(),
                                                               decoratedEvent.getStartDate(),
                                                               decoratedEvent.getEndDate());
            case MetsSplitter.INJECTED_TYPE_MIX -> handleMIX(decoratedEvent,
                                                             decoratedEvent.getAvis(),
                                                             decoratedEvent.getEditionDate(),
                                                             decoratedEvent.getUdgave(),
                                                             decoratedEvent.getSectionName(),
                                                             decoratedEvent.getPageNumber());
        }
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
//            System.out.println(x);
    
            Node metadataDC = xpath.selectNode(metsDoc,
                                               "/mets:mets/mets:dmdSec[@ID='DMD2']/mets:mdWrap/mets:xmlData/*");
            //            log.debug("DC metadata\n{}",XML.domToString(metadataDC));
            
            Node metadataMarc = xpath.selectNode(metsDoc,
                                                 "/mets:mets/mets:dmdSec[@ID='DMD3']/mets:mdWrap/mets:xmlData/*");
        } catch (ParserConfigurationException | SAXException | TransformerException e) {
            throw new IOException("Failed to parse METS data from " + decoratedEvent.getLocation(), e);
        }
    }
    
    private void handleMIX(DecoratedAttributeParsingEvent decoratedEvent,
                           String avis,
                           LocalDate editionDate,
                           String udgave,
                           String sectionName,
                           Integer pageNumber) {
        //        This is the mix extracted from METS for a specific page
        log.debug("Injected MIX event for {},{},{},{},{}", avis, editionDate, udgave, sectionName, pageNumber);
        
    }
}
