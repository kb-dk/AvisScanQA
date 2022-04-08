package dk.kb.kula190.checkers.batchcheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.Utils;
import dk.kb.kula190.checkers.batchcheckers.xpath.XpathDC;
import dk.kb.kula190.checkers.batchcheckers.xpath.XpathMarc;
import dk.kb.kula190.checkers.batchcheckers.xpath.XpathMods;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedNodeParsingEvent;
import dk.kb.util.json.JSON;
import dk.kb.util.xml.XML;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
    
    private ThreadLocal<XpathMods> metsMODS = new ThreadLocal<>();
    private ThreadLocal<XpathMods> standaloneMODS = new ThreadLocal<>();
    
    public MetsChecker(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    
    @Override
    public void modsFile(DecoratedAttributeParsingEvent event,
                         String newspaper,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {
        final XpathMods mods = new XpathMods(event,
                                             EventHandlerUtils.handleDocument(event),
                                             newspaper,
                                             roundTrip,
                                             startDate,
                                             endDate);
        this.standaloneMODS.set(mods);
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods digital origin should have been {expected} but was {actual}",
                    mods.getDigitalOrigin(),
                    "digitized newspaper");
        
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods internet media type should have been {expected} but was {actual}",
                    mods.getMimetypes(),
                    Set.of("text", "image/tif"));
        
        
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods physical description form should have been {expected} but was {actual}",
                    mods.getForm(),
                    "electronic");
        
        
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods start dates do not match date issued: {actual}, temporal: {expected}",
                    mods.getOriginDayIssuedStart(),
                    mods.getTemporalStart());
        
        
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods end dates do not match date issued: {actual}, temporal: {expected}",
                    mods.getOriginDayIssuedEnd(),
                    mods.getTemporalEnd());
        
    }
    
    @Override
    public void injectedFile(DecoratedAttributeParsingEvent decoratedEvent,
                             String injectedType,
                             String newspaper,
                             LocalDate editionDate,
                             String edition,
                             String section,
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
        log.trace("injected METS event for {},{},{},{}", avis, roundTrip, startDate, endDate);
        
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
            
            
            XpathMods mods = new XpathMods(decoratedEvent,
                                           Utils.asSeparateXML(xpath.selectNode(metsDoc,
                                                                                "/mets:mets/mets:dmdSec/mets:mdWrap[@MDTYPE='MODS']/mets:xmlData/*")),
                                           avis,
                                           roundTrip,
                                           startDate,
                                           endDate);
            metsMODS.set(mods);
            
            XpathDC dc = new XpathDC().data(decoratedEvent,
                                            Utils.asSeparateXML(xpath.selectNode(metsDoc,
                                                                                 "/mets:mets/mets:dmdSec/mets:mdWrap[@MDTYPE='DC']/mets:xmlData/*")),
                                            avis,
                                            roundTrip,
                                            startDate,
                                            endDate);
            
            
            XpathMarc marc = new XpathMarc().data(decoratedEvent,
                                                  Utils.asSeparateXML(xpath.selectNode(metsDoc,
                                                                                       "/mets:mets/mets:dmdSec/mets:mdWrap[@MDTYPE='MARC']/mets:xmlData/*")),
                                                  avis,
                                                  roundTrip,
                                                  startDate,
                                                  endDate);
            
            //Checks of "static" values
            checkMods(decoratedEvent, mods);
            
            checkDC(decoratedEvent, xpath, dc);
            
            checkMarc(decoratedEvent, marc);
            
            //Check of cross values between documents
            checkMarcMods(decoratedEvent, mods, marc);
            checkMarcModsDC(decoratedEvent, mods, dc, marc);
            
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
                       "Date start do not match throughout mets file  {0}, {1}, {2}",
                       metadataMarc.getMarc650a(),
                       metadataMods.getOriginDayIssuedStart(),
                       metadataMods.getTemporalStart());
        
        checkAllEquals(decoratedEvent,
                       FailureType.INVALID_METS_ERROR,
                       "Date start do not match throughout mets file  {0}, {1}, {2}",
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
        checkAllInSet(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets dc language was incorrect should have been some of {expected} but was {actual}",
                    metadataDC.getLanguage(),
                    Set.of("dan","ger"));
        
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
    
    private void checkModsVsMods(DecoratedNodeParsingEvent event, XpathMods modsFromMets, XpathMods modsStandalone) {
        final XpathMods actual = metsMODS.get();
        final XpathMods expected = standaloneMODS.get();
        checkEquals(event,
                    FailureType.INCONSISTENCY_ERROR,
                    "Mets-Mods data and Mods-file data is in disagreement. {actual}\nvs\n{expected}",
                    actual,
                    expected);
    }
    
    
    @Override
    public void tiffFile(DecoratedAttributeParsingEvent event,
                         String newspaper,
                         LocalDate editionDate,
                         String edition,
                         String section,
                         Integer pageNumber) throws IOException {
        tiffFilesVisited.add(EventHandlerUtils.lastName(event.getLocation()));
    }
    
    @Override
    public void altoFile(DecoratedAttributeParsingEvent event,
                         String newspaper,
                         LocalDate editionDate,
                         String edition,
                         String section,
                         Integer pageNumber) throws IOException {
        altoFilesVisited.add(EventHandlerUtils.lastName(event.getLocation()));
    }
    
    @Override
    public void batchEnds(DecoratedNodeParsingEvent event,
                          String newspaper,
                          String roundTrip,
                          LocalDate startDate,
                          LocalDate endDate) throws IOException {
        
        checkModsVsMods(event, metsMODS.get(), standaloneMODS.get());
        
        
        checkEquals(event,
                    FailureType.MISSING_FILE_ERROR,
                    "METS reference missing files not in batch: \n{actual}",
                    Utils.fromAnotInB(altoFilesFromMets, altoFilesVisited),
                    Set.of());
        checkEquals(event,
                    FailureType.MISSING_FILE_ERROR,
                    "Batch contains files not referenced in METS: \n{actual}",
                    Utils.fromAnotInB(altoFilesVisited, altoFilesFromMets),
                    Set.of());
        
        checkEquals(event,
                    FailureType.MISSING_FILE_ERROR,
                    "METS reference missing files: \n{actual}",
                    Utils.fromAnotInB(tiffFilesFromMets, tiffFilesVisited),
                    Set.of());
        checkEquals(event,
                    FailureType.MISSING_FILE_ERROR,
                    "Batch contains files not referenced in METS: \n{actual}",
                    Utils.fromAnotInB(tiffFilesVisited, tiffFilesFromMets),
                    Set.of());
        
        
    }
    
    
}
