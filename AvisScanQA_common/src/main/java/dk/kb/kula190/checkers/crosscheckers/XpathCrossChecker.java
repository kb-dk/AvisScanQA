package dk.kb.kula190.checkers.crosscheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.checkers.singlecheckers.TiffAnalyzer;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedNodeParsingEvent;
import dk.kb.util.xml.XML;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import dk.kb.util.yaml.YAML;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Objects;

import static dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils.lastName;
import static org.apache.commons.io.FilenameUtils.removeExtension;
/**
 * @see XpathAlto
 * @see XpathMix
 * @see XpathTiff
 */
public class XpathCrossChecker extends DecoratedEventHandler {
    public XpathCrossChecker(ResultCollector resultCollector) {
        super(resultCollector);
    }


    //The design:
    //A page consist of
    //with objects instead (at least) a tiff and a mix file

    //On page begin, we clear the state
    //We will then get a mixFileEvent and a tiffFileEvent. We do NOT know the order of these
    //When we have the relevant file, we extract the interesting properties
    //On page end, we KNOW we have visited both files
    //It is here we compare values between them

    private ThreadLocal<XpathAlto> Alto = new ThreadLocal<>();
    private ThreadLocal<XpathMix> Mix = new ThreadLocal<>();
    private ThreadLocal<XpathTiff> Tiff = new ThreadLocal<>();

    @Override
    public void pageBegins(DecoratedNodeParsingEvent event,
                           String avis,
                           LocalDate editionDate,
                           String udgave,
                           String sectionName,
                           Integer pageNumber) throws IOException {

        //clear state
        Mix.set(new XpathMix());
        Alto.set(new XpathAlto());
        Tiff.set(new XpathTiff());
    }

    @Override
    public void mixFile(DecoratedAttributeParsingEvent event,
                        String avis,
                        LocalDate editionDate,
                        String udgave,
                        String sectionName,
                        Integer pageNumber) throws IOException {
        //object
        Mix.get().setMixXpathData(event,avis,editionDate,udgave,sectionName,pageNumber);
    }

    @Override
    public void tiffFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        //object
        Tiff.get().setTiffXpathData(event,avis,editionDate,udgave,sectionName,pageNumber);
    }

    @Override
    public void altoFile(DecoratedAttributeParsingEvent event, String avis, LocalDate editionDate, String udgave, String sectionName, Integer pageNumber) throws IOException {
        //object
        Alto.get().setAltoXpathData(event,avis,editionDate,udgave,sectionName,pageNumber);
    }

    @Override
    public void injectedFile(DecoratedAttributeParsingEvent decoratedEvent, String injectedType, String avis, LocalDate editionDate, String udgave, String sectionName, Integer pageNumber) throws IOException {
        //object
        Tiff.get().setTiffInjectedFileData(decoratedEvent,injectedType,avis,editionDate,udgave,sectionName,pageNumber);

    }

    @Override
    public void pageEnds(DecoratedNodeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) {
        checkEquals(event, "TIFF_MIX_ERROR", "mix metadata (file size: {expected}) does not match actual tif file size {actual}", Tiff.get().getTifSizeActual(), Mix.get().getTifSizePerMix()); //TODO values in descriptin

        checkEquals(event, "TIFF_MIX_ERROR", "mix metadata (checksum {expected}) does not match actual tif file checksum {actual}", Tiff.get().getChecksumTif(),Mix.get().getChecksumMix());

        //checkAllEquals(event,"CROSS_ERROR","mix metadata (file size: {val1}) tif metadata (file size {val2}) alto");
        checkAllEquals(event,"CROSS_ERROR","mix metadata (filename: {val1}) tif metadata (filename: {val2}) alto metadata (filename: {val3}) one does not match", new String[]{Mix.get().getMixFileName(), Tiff.get().getTifFileName(), Alto.get().getAltoFileName()});
        checkAllEquals(event,"CROSS_ERROR","mix metadata (image height: {val1}) tif metadata (image height: {val2}) alto metadata (image height: {val3}) one does not match", new Integer[]{Mix.get().getMixImageHeight(), Tiff.get().getImageHeightTif(), Alto.get().getAltoImageHeight()});
        checkAllEquals(event,"CROSS_ERROR","mix metadata (image width: {val1}) tif metadata (image width: {val2}) alto metadata (image width: {val3}) one does not match", new Integer[]{Mix.get().getMixImageWidth(), Tiff.get().getImageWidthTif(), Alto.get().getAltoImageWidth()});


    }
}
