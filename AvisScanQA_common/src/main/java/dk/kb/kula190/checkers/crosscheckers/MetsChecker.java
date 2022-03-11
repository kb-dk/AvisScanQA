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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
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
    
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods digital origin should have been {expected} but was {actual}",
                    xpath.selectString(document, "/mods:mods/mods:physicalDescription/mods:digitalOrigin"),
                    "digitized newspaper"
                   );
    
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods internet media type should have been {expected} but was {actual}",
                    new HashSet<String>(xpath.selectStringList(document,
                                                               "/mods:mods/mods:physicalDescription/mods:internetMediaType/text()")),
                    Set.of("text", "image/tif")
                   );
    
    
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods physical description form should have been {expected} but was {actual}",
                    xpath.selectString(document, "/mods:mods/mods:physicalDescription/mods:form/text()"),
                    "electronic"
                   );
    
    
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods start dates do not match date issued: {actual}, temporal: {expected}",
                    xpath.selectString(document,
                                                                "/mods:mods/mods:originInfo/mods:dateIssued[@point='start']"),
                    xpath.selectString(document, "/mods:mods/mods:subject/mods:temporal[@point='start']"));
    
    
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods end dates do not match date issued: {actual}, temporal: {expected}",
                    xpath.selectString(document, "/mods:mods/mods:originInfo/mods:dateIssued[@point='end']"),
                    xpath.selectString(document, "/mods:mods/mods:subject/mods:temporal[@point='end']"));
    
        checkEquals(event,
                    FailureType.INVALID_MODS_ERROR,
                    "Mods file family was incorrect should have been {expected} but was {actual}",
                    xpath.selectString(document, "/mods:mods/mods:identifier[@type='title_family']"),
                    event.getName().split("_")[0]);
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
                                                             "mods", "http://www.loc.gov/mods/v3",
                                                             "oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/",
                                                             "marc", "http://www.loc.gov/MARC21/slim");
        
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
            Node metadataMods = asSeparateXML(xpath.selectNode(metsDoc,
                                                               "/mets:mets/mets:dmdSec[@ID='DMD1']/mets:mdWrap/mets:xmlData/*"));
            
            Node metadataDC = asSeparateXML(xpath.selectNode(metsDoc,
                                                             "/mets:mets/mets:dmdSec[@ID='DMD2']/mets:mdWrap/mets:xmlData/*"));
            
            Node metadataMarc = asSeparateXML(xpath.selectNode(metsDoc,
                                                               "/mets:mets/mets:dmdSec[@ID='DMD3']/mets:mdWrap/mets:xmlData/*"));
            
            checkMods(decoratedEvent, xpath, metadataMods);
    
            checkDC(decoratedEvent, xpath, metadataDC);
    
            checkMarc(decoratedEvent, xpath, metadataMarc);
    
            checkMarcMods(decoratedEvent, xpath, metadataMods, metadataMarc);
    
            checkMarcModsDC(decoratedEvent, xpath, metadataMods, metadataDC, metadataMarc);
    
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse METS data from " + decoratedEvent.getLocation(), e);
        }
    }
    
    private void checkMarcModsDC(DecoratedAttributeParsingEvent decoratedEvent,
                           XPathSelector xpath,
                           Node metadataMods,
                           Node metadataDC,
                           Node metadataMarc) {
        //Marc Mods DC
        
        Set<String> titleDCSet = new HashSet<>(xpath.selectStringList(metadataDC, "/oai_dc:dc/dc:title"));
        String titleUniformDC = titleDCSet.stream().max(Comparator.comparingInt(String::length)).get();
        String titleDC = titleDCSet.stream().min(Comparator.comparingInt(String::length)).get();
        
        checkAllEquals(decoratedEvent,
                       FailureType.INVALID_METS_ERROR,
                       "Title was not the same throughout mets file: {val1}, {val2}, {val3}",
                       titleDC,
                       xpath.selectString(metadataMarc,
                                          "/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='a']"),
                       xpath.selectString(metadataMods, "/mods:mods/mods:titleInfo[not(@*)]/mods:title"));
        checkAllEquals(decoratedEvent,
                       FailureType.INVALID_METS_ERROR,
                       "Title uniform was not the same throughout mets file: {val1}, {val2}, {val3}",
                       titleUniformDC,
                       xpath.selectString(metadataMarc,
                                          "/marc:record/marc:datafield[@tag='130']/marc:subfield[@code='a']"),
                       xpath.selectString(metadataMods,
                                          "/mods:mods/mods:titleInfo[@type='uniform']/mods:title"));
        
        checkAllEquals(decoratedEvent,
                       FailureType.INVALID_METS_ERROR,
                       "Location throughout mets file do no match: {val1}, {val2}, {val3}",

                       xpath.selectString(metadataMarc,
                                          "/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='a']"),
                       xpath.selectString(metadataMods,
                                          "/mods:mods/mods:subject/mods:hierarchicalGeographic/mods:city"),
                       xpath.selectString(metadataMods, "/mods:mods/mods:originInfo/mods:place/mods:placeTerm")
                      );
    }
    
    private void checkMarcMods(DecoratedAttributeParsingEvent decoratedEvent,
                           XPathSelector xpath,
                           Node metadataMods,
                           Node metadataMarc) {
        //Marc Mods
        
        checkAllEquals(decoratedEvent, FailureType.INVALID_METS_ERROR,
                       "Mets date start do not match throughout mets file {val1},{val2},{val3}",
                       xpath.selectString(metadataMarc,
                                          "/marc:record/marc:datafield[@tag='650']/marc:subfield[@code='a']"),
                       xpath.selectString(metadataMods,
                                          "/mods:mods/mods:originInfo/mods:dateIssued[@point='start']"),
                       xpath.selectString(metadataMods, "/mods:mods/mods:subject/mods:temporal[@point='start']")
                      );
        checkAllEquals(decoratedEvent, FailureType.INVALID_METS_ERROR,
                       "Mets date start do not match throughout mets file {val1},{val2},{val3}",

                       xpath.selectString(metadataMarc,
                                          "/marc:record/marc:datafield[@tag='650']/marc:subfield[@code='y']"),
                       xpath.selectString(metadataMods,
                                          "/mods:mods/mods:originInfo/mods:dateIssued[@point='end']"),
                       xpath.selectString(metadataMods,
                                          "/mods:mods/mods:subject/mods:temporal[@point='end']")
                      );
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets issuance should have been {expected} but was {actual}",
                    xpath.selectString(metadataMarc,
                                       "/marc:record/marc:datafield[@tag='250']/marc:subfield[@code='a']"),
                    xpath.selectString(metadataMods, "/mods:mods/mods:originInfo/mods:issuance")
                   );
    }
    
    private void checkMarc(DecoratedAttributeParsingEvent decoratedEvent, XPathSelector xpath, Node metadataMarc) {
        // MARC
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets file {expected} was instead {actual}",
                    xpath.selectString(metadataMarc,
                                       "/marc:record/marc:datafield[@tag='250']/marc:subfield[@code='a']"),
                    "serial");
    }
    
    private void checkDC(DecoratedAttributeParsingEvent decoratedEvent, XPathSelector xpath, Node metadataDC) {
        //DC
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets dc language was incorrect should have been {expected} but was {actual}",
                    xpath.selectString(metadataDC, "/oai_dc:dc/dc:language"),
                    "dan");
        
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets dc format should have been {expected} but was {actual}",
                    new HashSet<>(xpath.selectStringList(metadataDC, "/oai_dc:dc/dc:format")),
                    Set.of("text", "image/tif", "electronic")
                   );
        
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets dc type should have been {expected} but was {actual}",
                    new HashSet<>(xpath.selectStringList(metadataDC, "/oai_dc:dc/dc:type")),
                    Set.of("newspaper", "text")
                   );
        
        checkTrue(decoratedEvent,
                  FailureType.INVALID_METS_ERROR,
                  "Mets dc date ({actual}) is not within dc coverage ({expected})",
                  (
                          (Set<String>) new HashSet<>(xpath.selectStringList(metadataDC,
                                                                             "/oai_dc:dc/dc:coverage"))).contains(
                          xpath.selectString(metadataDC, "/oai_dc:dc/dc:type")));
    }
    
    private void checkMods(DecoratedAttributeParsingEvent decoratedEvent, XPathSelector xpath, Node metadataMods) {
        //MODS
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets issuance should have been {expected} but was {actual}",
                    xpath.selectString(metadataMods, "/mods:mods/mods:originInfo/mods:issuance"),
                    "serial"
                   );
        
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets digital origin should have been {expected} but was {actual}",
                    xpath.selectString(metadataMods, "/mods:mods/mods:physicalDescription/mods:digitalOrigin"),
                    "digitized newspaper"
                   );
        
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets internet media type should have been {expected} but was {actual}",
                    new HashSet<>(xpath.selectStringList(metadataMods,
                                                         "/mods:mods/mods:physicalDescription/mods:internetMediaType/text()")),
                    Set.of("text", "image/tif")
                   );
        
        
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets physical description form should have been {expected} but was {actual}",
                    xpath.selectString(metadataMods, "/mods:mods/mods:physicalDescription/mods:form/text()"),
                    "electronic"
                   );
        
        
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets start dates do not match date issued: {actual}, temporal: {expected}",
                    xpath.selectString(metadataMods,
                                       "/mods:mods/mods:originInfo/mods:dateIssued[@point='start']"),
                    xpath.selectString(metadataMods, "/mods:mods/mods:subject/mods:temporal[@point='start']"));
        
        
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets end dates do not match date issued: {actual}, temporal: {expected}",
                    xpath.selectString(metadataMods,
                                       "/mods:mods/mods:originInfo/mods:dateIssued[@point='end']"),
                    xpath.selectString(metadataMods, "/mods:mods/mods:subject/mods:temporal[@point='end']"));
        
        checkEquals(decoratedEvent,
                    FailureType.INVALID_METS_ERROR,
                    "Mets file family was incorrect should have been {expected} but was {actual}",
                    xpath.selectString(metadataMods, "/mods:mods/mods:identifier[@type='title_family']"),
                    decoratedEvent.getName().split("_")[0]);
    }
    
    private Node asSeparateXML(Node metadataMods) throws ParserConfigurationException {
        Document document = DocumentBuilderFactory.newInstance()
                                                  .newDocumentBuilder()
                                                  .newDocument();
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
