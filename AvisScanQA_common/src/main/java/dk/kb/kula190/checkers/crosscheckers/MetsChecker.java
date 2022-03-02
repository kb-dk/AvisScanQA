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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class MetsChecker extends DecoratedEventHandler {
    
    
    private Set<String> tiffFilesVisited = Collections.synchronizedSet(new HashSet<>());
    private Set<String> altoFilesVisited = Collections.synchronizedSet(new HashSet<>());
    
    private Set<String> tiffFilesFromMets = Collections.synchronizedSet(new HashSet<>());
    private Set<String> altoFilesFromMets = Collections.synchronizedSet(new HashSet<>());
    
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
                                                             "xlink", "http://www.w3.org/1999/xlink",
                                                             "mods", "http://www.loc.gov/mods/v3");
        try (InputStream data = decoratedEvent.getData()) {
            Document metsDoc = XML.fromXML(data, true);
            
            //This is the  METS file, without the humongous ADMSEC part
            String x = XML.domToString(metsDoc);
            
            //            TODO check that DC, MARC and MODS are in agreement
            
            Node metadataMods = asSeparateXML(xpath.selectNode(metsDoc,
                                                 "/mets:mets/mets:dmdSec[@ID='DMD1']/mets:mdWrap/mets:xmlData/*"));

            String x1 = xpath.selectString(metadataMods, "/mods:mods/mods:titleInfo/mods:title");
            System.out.println(x1);
            //TODO check this with same checks as in ModsChecker
            
            
            Node metadataDC = asSeparateXML(xpath.selectNode(metsDoc,
                                               "/mets:mets/mets:dmdSec[@ID='DMD2']/mets:mdWrap/mets:xmlData/*"));
            //            log.debug("DC metadata\n{}",XML.domToString(metadataDC));
            
    
            Node metadataMarc = asSeparateXML(xpath.selectNode(metsDoc,
                                                 "/mets:mets/mets:dmdSec[@ID='DMD3']/mets:mdWrap/mets:xmlData/*"));
            
            
            // Save the filelists for later
            xpath.selectStringList(metsDoc,
                                   "/mets:mets/mets:fileSec/mets:fileGrp[@ID='TIFF']/mets:file/mets:FLocat/@xlink:href")
                 .stream()
                 .map(ref -> ref.replaceAll(Pattern.quote("\\"), File.separator))
                 .map(ref -> new File(ref).getName())
                 .forEach(ref -> tiffFilesFromMets.add(ref));
            
            
            xpath.selectStringList(metsDoc,
                                   "/mets:mets/mets:fileSec/mets:fileGrp[@ID='ALTO']/mets:file/mets:FLocat/@xlink:href")
                 .stream()
                 .map(ref -> ref.replaceAll(Pattern.quote("\\"), File.separator))
                 .map(ref -> new File(ref).getName())
                 .forEach(ref -> altoFilesFromMets.add(ref));
            
            
        } catch (ParserConfigurationException | SAXException | TransformerException e) {
            throw new IOException("Failed to parse METS data from " + decoratedEvent.getLocation(), e);
        }
    }
    
    private Node asSeparateXML(Node metadataMods) throws ParserConfigurationException {
        Document document = DocumentBuilderFactory.newInstance()
                                                  .newDocumentBuilder()
                                                  .newDocument();
        Node mods = document.appendChild(document.adoptNode(metadataMods));
        return mods;
    }
    
    @Override
    public void tiffFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        tiffFilesVisited.add(EventHandlerUtils.lastName(event.getLocation()));
    }
    
    @Override
    public void altoFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        altoFilesVisited.add(EventHandlerUtils.lastName(event.getLocation()));
    }
    
    @Override
    public void batchEnds(DecoratedNodeParsingEvent event,
                          String avis,
                          String roundTrip,
                          LocalDate startDate,
                          LocalDate endDate) throws IOException {
        
        checkEquals(event, FailureType.MISSING_FILE_ERROR, "METS reference missing files not in batch: \n{actual}",
                    fromAnotInB(altoFilesFromMets, altoFilesVisited), Set.of());
        checkEquals(event, FailureType.MISSING_FILE_ERROR, "Batch contains files not referenced in METS: \n{actual}",
                    fromAnotInB(altoFilesVisited, altoFilesFromMets), Set.of());
        
        checkEquals(event, FailureType.MISSING_FILE_ERROR, "METS reference missing files: \n{actual}",
                    fromAnotInB(tiffFilesFromMets, tiffFilesVisited), Set.of());
        checkEquals(event, FailureType.MISSING_FILE_ERROR, "Batch contains files not referenced in METS: \n{actual}",
                    fromAnotInB(tiffFilesVisited, tiffFilesFromMets), Set.of());
        
        
    }
    
    private Set<String> fromAnotInB(Set<String> altoFilesFromMets, Set<String> altoFilesVisited) {
        HashSet<String> diff = new HashSet<>(altoFilesFromMets);
        diff.removeAll(
                altoFilesVisited);
        return diff;
    }
    
}
