package dk.kb.kula190.checkers.crosscheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedNodeParsingEvent;
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

import static dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils.lastName;
import static org.apache.commons.io.FilenameUtils.removeExtension;

public class MixAltoChecker extends DecoratedEventHandler {
    public MixAltoChecker(ResultCollector resultCollector) {
        super(resultCollector);
    }

    //The design:
    //A page consist of (at least) an alto and a mix file

    //On page begin, we clear the state
    //We will then get a mixFileEvent and an altoFileEvent. We do NOT know the order of these
    //When we have the relevant file, we extract the interesting properties
    //On page end, we KNOW we have visited both files
    //It is here we compare values between them


    //part of the state. This is the size of the tif file, as reported by mix
    private ThreadLocal<String> MixFileName = new ThreadLocal<>();
    private ThreadLocal<String> AltoFileName = new ThreadLocal<>();



    @Override
    public void pageBegins(DecoratedNodeParsingEvent event,
                           String avis,
                           LocalDate editionDate,
                           String udgave,
                           String sectionName,
                           Integer pageNumber) throws IOException {
        //clear the state
        MixFileName.set(null);
        AltoFileName.set(null);


    }

    @Override
    public void mixFile(DecoratedAttributeParsingEvent event,
                        String avis,
                        LocalDate editionDate,
                        String udgave,
                        String sectionName,
                        Integer pageNumber) throws IOException {
        Document document = handleDocument(event);

        XPathSelector xpath = XpathUtils.createXPathSelector("mix", "http://www.loc.gov/mix/v20");


        String fileName = xpath.selectString(document,
                "/mix:mix/mix:BasicDigitalObjectInformation/mix:ObjectIdentifier/mix:objectIdentifierValue");
        MixFileName.set(removeExtension(lastName(fileName)));
    }

    @Override
    public void altoFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        Document document = handleDocument(event);
        XPathSelector xpath = XpathUtils.createXPathSelector("mix", "http://www.loc.gov/mix/v20");


        String fileName = xpath.selectString(document,
                "/alto/Description/sourceImageInformation/fileName");
        AltoFileName.set(removeExtension(lastName(fileName)));

    }

    @Override
    public void pageEnds(DecoratedNodeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) {
        checkEquals(event,"MIX_ALTO_CROSS_ERROR",MixFileName.get(),AltoFileName.get(),"Filename on mix and alto are not the same Mix: {actual} Alto: {expected}");

    }
    private Document handleDocument(DecoratedAttributeParsingEvent event) throws IOException {
        Document document;
        try (InputStream in = event.getData()) {
            document = XML.fromXML(in, true);
            return document;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException | IOException e) {
            throw new IOException(e);
        }
    }
}
