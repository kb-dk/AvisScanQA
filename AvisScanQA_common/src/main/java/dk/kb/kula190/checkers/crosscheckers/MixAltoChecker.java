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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    private ThreadLocal<Integer> AltoImageHeight = new ThreadLocal<>();
    private ThreadLocal<Integer> AltoImageWidth = new ThreadLocal<>();
    private ThreadLocal<Integer> MixImageHeight = new ThreadLocal<>();
    private ThreadLocal<Integer> MixImageWidth = new ThreadLocal<>();



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
        AltoImageHeight.set(null);
        AltoImageWidth.set(null);
        MixImageHeight.set(null);
        MixImageWidth.set(null);

    }

    @Override
    public void mixFile(DecoratedAttributeParsingEvent event,
                        String avis,
                        LocalDate editionDate,
                        String udgave,
                        String sectionName,
                        Integer pageNumber) throws IOException {
        Document document = handleDocument(event); // error with this, why doesn't this work...

        XPathSelector xpath = XpathUtils.createXPathSelector("mix", "http://www.loc.gov/mix/v20");


        String fileName = xpath.selectString(document,
                "/mix:mix/mix:BasicDigitalObjectInformation/mix:ObjectIdentifier/mix:objectIdentifierValue");
        MixFileName.set(removeExtension(lastName(fileName)));

        Integer mixHeight = xpath.selectInteger(document,"/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageHeight");
        MixImageHeight.set(mixHeight);
        Integer mixWidth = xpath.selectInteger(document,"/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageWidth");
        MixImageWidth.set(mixWidth);

    }

    @Override
    public void altoFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        Document document = handleDocument(event);
        XPathSelector xpath = XpathUtils.createXPathSelector("alto", "http://www.loc.gov/standards/alto/ns-v2#");


        String fileName = xpath.selectString(document,
                "/alto:alto/alto:Description/alto:sourceImageInformation/alto:fileName");
        AltoFileName.set(removeExtension(lastName(fileName)));


        //TODO alto: before each in that xpath
        List<String> lines = Arrays.stream(xpath.selectString(document, "/alto:alto/alto:Description/alto:OCRProcessing/alto:ocrProcessingStep/alto:processingStepSettings").split("\n")).toList();

        //Line is "width:2180" | gets -1
        Integer width = Integer.parseInt(lines.stream()
                                              .filter(line -> line.startsWith("width:"))
                                              .map(line -> line.split(":", 2)[1].trim())
                                              .findFirst()
                                              .orElse("-1"));
        AltoImageWidth.set(width);
        //Line is "height:2786" | gets -1
        Integer height = Integer.parseInt(lines.stream()
                                               .filter(line -> line.startsWith("height:"))
                                               .map(line -> line.split(":", 2)[1].trim())
                                               .findFirst()
                                               .orElse("-1"));
        AltoImageHeight.set(height);
        //ALTO PAGE HEIGHT / (ALTO MEASUREMENT UNIT / DPI) = TIFF HEIGHT

        //DPI from MIX or TIFF METADATA

    }

    @Override
    public void pageEnds(DecoratedNodeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) {
        checkEquals(event,"MIX_ALTO_CROSS_ERROR",MixFileName.get(),AltoFileName.get(),"Filename on mix and alto are not the same Mix: {actual} Alto: {expected}");
        checkEquals(event,"MIX_ALTO_CROSS_ERROR",MixImageWidth.get(),AltoImageWidth.get(),"Mix image width was {actual} Alto image width was {expected}");
        checkEquals(event,"MIX_ALTO_CROSS_ERROR",MixImageHeight.get(),AltoImageHeight.get(),"Mix image height was {actual} Alto image height was {expected}");



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
