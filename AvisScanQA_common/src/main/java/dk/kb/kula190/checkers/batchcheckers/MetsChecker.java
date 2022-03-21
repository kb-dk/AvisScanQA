package dk.kb.kula190.checkers.batchcheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.checkers.batchcheckers.xpath.XpathDC;
import dk.kb.kula190.checkers.batchcheckers.xpath.XpathMarc;
import dk.kb.kula190.checkers.batchcheckers.xpath.XpathMods;
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
    
    private ThreadLocal<XpathMods> xpathMetsMods = new ThreadLocal<>();
    private ThreadLocal<XpathDC> xpathMetsDC = new ThreadLocal<>();
    private ThreadLocal<XpathMarc> xpathMetsMarc = new ThreadLocal<>();
    private ThreadLocal<XpathMods> xpathMods = new ThreadLocal<>();
    
    public MetsChecker(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    @Override
    public void batchBegins(DecoratedNodeParsingEvent event,
                            String avis,
                            String roundTrip,
                            LocalDate startDate,
                            LocalDate endDate) throws IOException {
        xpathMods.set(new XpathMods());
        xpathMetsMods.set(new XpathMods());
        xpathMetsDC.set(new XpathDC());
        xpathMetsMarc.set(new XpathMarc());
    }
    
    @Override
    public void modsFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {
        XpathMods xpathMods = this.xpathMods.get();
        xpathMods.setModsDAta(event, EventHandlerUtils.handleDocument(event), avis, roundTrip, startDate, endDate);
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods digital origin should have been {expected} but was {actual}",
                    xpathMods.getDigitalOrigin(),
                    "digitized newspaper");
        
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods internet media type should have been {expected} but was {actual}",
                    xpathMods.getMimetypes(),
                    Set.of("text", "image/tif"));
        
        
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods physical description form should have been {expected} but was {actual}",
                    xpathMods.getForm(),
                    "electronic");
        
        
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods start dates do not match date issued: {actual}, temporal: {expected}",
                    xpathMods.getOriginDayIssuedStart(),
                    xpathMods.getTemporalStart());
        
        
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods end dates do not match date issued: {actual}, temporal: {expected}",
                    xpathMods.getOriginDayIssuedEnd(),
                    xpathMods.getTemporalEnd());
        
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
    
    private void handleMETS(DecoratedAttributeParsingEvent decoratedEvent,
                            String avis,
                            String roundTrip,
                            LocalDate startDate,
                            LocalDate endDate) throws IOException {
        log.debug("injected METS event for {},{},{},{}", avis, roundTrip, startDate, endDate);
        
        XPathSelector xpath =
                XpathUtils.createXPathSelector(
                        "mets", "http://www.loc.gov/METS/",
                        "xlink", "http://www.w3.org/1999/xlink"
                        );
        
        try (InputStream data = decoratedEvent.getData()) {
            Document metsDoc = XML.fromXML(data, true);
            
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
            
            
            //TODO is the Type2DMD_Num always the same? Is Mods always DMD1?
            
            XpathMods metsMods = this.xpathMetsMods.get();
            metsMods.setModsDAta(decoratedEvent,
                                 asSeparateXML(xpath.selectNode(metsDoc,
                                                                "/mets:mets/mets:dmdSec[@ID='DMD1']/mets:mdWrap/mets:xmlData/*")),
                                 avis,
                                 roundTrip,
                                 startDate,
                                 endDate);
            
            
            XpathDC dc = this.xpathMetsDC.get();
            dc.setMetsDCInjectedFileData(decoratedEvent,
                                         asSeparateXML(xpath.selectNode(metsDoc,
                                                                        "/mets:mets/mets:dmdSec[@ID='DMD2']/mets:mdWrap/mets:xmlData/*")),
                                         avis,
                                         roundTrip,
                                         startDate,
                                         endDate);
            
            
            XpathMarc marc = this.xpathMetsMarc.get();
            marc.setMetsMarcInjectedFileData(decoratedEvent,
                                             asSeparateXML(xpath.selectNode(metsDoc,
                                                                            "/mets:mets/mets:dmdSec[@ID='DMD3']/mets:mdWrap/mets:xmlData/*")),
                                             avis,
                                             roundTrip,
                                             startDate,
                                             endDate);
            //Node metadataMix = asSeparateXML(xpath.selectNode(metsDoc,"/mets:mets/mets:amdSec[]"));
            
            checkMods(decoratedEvent, metsMods);
            
            checkDC(decoratedEvent, xpath, dc);
            
            checkMarc(decoratedEvent, marc);
            
            checkMarcMods(decoratedEvent, metsMods, marc);
            
            checkMarcModsDC(decoratedEvent, metsMods, dc, marc);
            
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse METS data from " + decoratedEvent.getLocation(), e);
        }
    }
    
    private void checkMarcModsDC(DecoratedAttributeParsingEvent decoratedEvent,
                                 XpathMods metadataMods,
                                 XpathDC metadataDC,
                                 XpathMarc metadataMarc) {
        //Marc Mods DC
        
        Set<String> dcTitles = metadataDC.getTitles();
        Set<String> marcTitles = Set.of(metadataMarc.getMarc245a(), metadataMarc.getMarc130a());
        Set<String> modsTitles = metadataMods.getTitles();
        
        
        checkAllEquals(decoratedEvent,
                       FailureType.INVALID_METS_ERROR,
                       "Title was not the same throughout mets file: {0}, {1}, {2}",
                       dcTitles, marcTitles, modsTitles);
        
        checkAllEquals(decoratedEvent,
                       FailureType.INVALID_METS_ERROR,
                       "Location throughout mets file do no match:  {0}, {1}, {2}",
                       metadataMarc.getMarc260a(),
                       metadataMods.getSubjectCity(),
                       metadataMods.getOriginPlace());
    }
    
    private void checkMarcMods(DecoratedAttributeParsingEvent decoratedEvent,
                               XpathMods metadataMods,
                               XpathMarc metadataMarc) {
        checkAllEquals(decoratedEvent,
                       FailureType.INVALID_METS_ERROR,
                       "Mets date start do not match throughout mets file  {0}, {1}, {2}",
                       metadataMarc.getMarc650a(),
                       metadataMods.getOriginDayIssuedStart(),
                       metadataMods.getTemporalStart());
        
        checkAllEquals(decoratedEvent,
                       FailureType.INVALID_METS_ERROR,
                       "Mets date start do not match throughout mets file  {0}, {1}, {2}",
                       metadataMarc.getMarc650y(),
                       metadataMods.getOriginDayIssuedEnd(),
                       metadataMods.getTemporalEnd());
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets issuance should have been {expected} but was {actual}",
                    metadataMarc.getMarc250a(),
                    metadataMods.getOriginIssuance());
        
    }
    
    private void checkMarc(DecoratedAttributeParsingEvent decoratedEvent,
                           XpathMarc metadataMarc) {
        // MARC
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets file {expected} was instead {actual}",
                    metadataMarc.getMarc250a(),
                    "serial");
    }
    
    private void checkDC(DecoratedAttributeParsingEvent decoratedEvent, XPathSelector xpath, XpathDC metadataDC) {
        //DC
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets dc language was incorrect should have been {expected} but was {actual}",
                    metadataDC.getLanguage(),
                    "dan");
        
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets dc format should have been {expected} but was {actual}",
                    metadataDC.getFormats(),
                    Set.of("text", "image/tif", "electronic"));
        
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets dc type should have been {expected} but was {actual}",
                    metadataDC.getTypes(),
                    Set.of("newspaper", "text"));
        
        
        checkInSet(decoratedEvent,
                   FailureType.INVALID_METS_ERROR,
                   "Mets dc date ({0}) is not within dc coverage ({0})",
                   metadataDC.getDate(),
                   metadataDC.getCoverages()
                  );
        
    }
    
    private void checkMods(DecoratedAttributeParsingEvent decoratedEvent, XpathMods metadataMods) {
        //MODS
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets issuance should have been {expected} but was {actual}",
                    metadataMods.getOriginIssuance(),
                    "serial");
        
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets digital origin should have been {expected} but was {actual}",
                    metadataMods.getDigitalOrigin(),
                    "digitized newspaper");
        
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets internet media type should have been {expected} but was {actual}",
                    metadataMods.getMimetypes(),
                    Set.of("text", "image/tif"));
        
        
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets start dates do not match date issued: {actual}, temporal: {expected}",
                    metadataMods.getOriginDayIssuedStart(),
                    metadataMods.getTemporalStart());
        
        
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets end dates do not match date issued: {actual}, temporal: {expected}",
                    metadataMods.getOriginDayIssuedEnd(),
                    metadataMods.getTemporalEnd());
    }
    
    private Node asSeparateXML(Node metadataMods) throws ParserConfigurationException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node mods = document.appendChild(document.adoptNode(metadataMods));
        return document.getDocumentElement();
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
        
        checkEquals(event,
                    FailureType.MISSING_FILE_ERROR,
                    "METS reference missing files not in batch: \n{actual}",
                    fromAnotInB(altoFilesFromMets, altoFilesVisited),
                    Set.of());
        checkEquals(event,
                    FailureType.MISSING_FILE_ERROR,
                    "Batch contains files not referenced in METS: \n{actual}",
                    fromAnotInB(altoFilesVisited, altoFilesFromMets),
                    Set.of());
        
        checkEquals(event,
                    FailureType.MISSING_FILE_ERROR,
                    "METS reference missing files: \n{actual}",
                    fromAnotInB(tiffFilesFromMets, tiffFilesVisited),
                    Set.of());
        checkEquals(event,
                    FailureType.MISSING_FILE_ERROR,
                    "Batch contains files not referenced in METS: \n{actual}",
                    fromAnotInB(tiffFilesVisited, tiffFilesFromMets),
                    Set.of());
        
        
    }
    
    private Set<String> fromAnotInB(Set<String> altoFilesFromMets, Set<String> altoFilesVisited) {
        HashSet<String> diff = new HashSet<>(altoFilesFromMets);
        diff.removeAll(altoFilesVisited);
        return diff;
    }
    
}
