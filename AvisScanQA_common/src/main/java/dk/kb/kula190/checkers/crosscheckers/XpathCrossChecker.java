package dk.kb.kula190.checkers.crosscheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.checkers.singlecheckers.MetsSplitter;
import dk.kb.kula190.checkers.singlecheckers.TiffAnalyzerImageMagick;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedNodeParsingEvent;

import java.io.IOException;
import java.time.LocalDate;

/**
 * @see XpathAlto for how values are extracted from ALTO
 * @see XpathMix for how values are extracted from MIX
 * @see XpathTiff for how values are extracted from TIFF
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
    private ThreadLocal<XpathMetsMix> MetsMix = new ThreadLocal<>();

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
        MetsMix.set(new XpathMetsMix());
    }

    @Override
    public void mixFile(DecoratedAttributeParsingEvent event,
                        String avis,
                        LocalDate editionDate,
                        String udgave,
                        String sectionName,
                        Integer pageNumber) throws IOException {
        //object
        XpathMix xpathMix = Mix.get();
        xpathMix.setMixXpathData(event, avis, editionDate, udgave, sectionName, pageNumber);

    }

    @Override
    public void tiffFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        //object
        XpathTiff xpathTiff = Tiff.get();
        xpathTiff.setTiffXpathData(event, avis, editionDate, udgave, sectionName, pageNumber);
    }

    @Override
    public void altoFile(DecoratedAttributeParsingEvent event, String avis, LocalDate editionDate, String udgave,
                         String sectionName, Integer pageNumber) throws IOException {

        XpathAlto xpathAlto = Alto.get();
        xpathAlto.setAltoXpathData(event, avis, editionDate, udgave, sectionName, pageNumber);
    }

    @Override
    public void injectedFile(DecoratedAttributeParsingEvent decoratedEvent, String injectedType, String avis,
                             LocalDate editionDate, String udgave, String sectionName, Integer pageNumber)
            throws IOException {

        switch (injectedType) {
            case TiffAnalyzerImageMagick.INJECTED_TYPE -> { // ImageMagick Output
                XpathTiff xpathTiff = Tiff.get();
                xpathTiff.setTiffInjectedFileData(decoratedEvent, injectedType, avis, editionDate, udgave, sectionName,
                                                  pageNumber);
            }
            case MetsSplitter.INJECTED_TYPE_MIX -> { //MIX FROM METS FILE
                //        This is the mix extracted from METS for a specific page
                log.debug("Injected MIX event for {},{},{},{},{}", avis, editionDate, udgave, sectionName, pageNumber);
                XpathMetsMix xpathMetsMix = MetsMix.get();
                xpathMetsMix.setMetsMixInjectedFileData(decoratedEvent,
                                                        injectedType,
                                                        avis,
                                                        editionDate,
                                                        udgave,
                                                        sectionName,
                                                        pageNumber);
            }
        }
    }

    @Override
    public void pageEnds(DecoratedNodeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) {
        XpathMix xpathMix = Mix.get();
        XpathTiff xpathTiff = Tiff.get();
        XpathAlto xpathAlto = Alto.get();
        XpathMetsMix xpathMetsMix = MetsMix.get();
        metsMixAndMixCheck(event, xpathMetsMix, xpathMix);
        boolean injectedDataSupplied = xpathTiff.isInjectedDataSupplied();

        checkTrue(event,
                  FailureType.TIFF_ANALYZE_ERROR,
                  "Imagemagick metadata for tiff not supplied",
                  injectedDataSupplied);


        checkEquals(event,
                    FailureType.TIFF_MIX_ERROR,
                    "mix metadata (file size: {expected}) does not match actual tif file " +
                    "size {actual}",
                    xpathTiff.getTifSizeActual(),
                    xpathMix.getTifSizePerMix());

        checkEquals(event,
                    FailureType.TIFF_MIX_ERROR,
                    "mix metadata (checksum {expected}) does not match actual tif file " +
                    "checksum {actual}",
                    xpathTiff.getChecksumTif(),
                    xpathMix.getChecksumMix());

        checkAllEquals(event,
                       FailureType.CROSS_ERROR,
                       "mix metadata (filename: {val1}) tif metadata (filename: {val2}) alto " +
                       "metadata (filename: {val3}) one does not match",
                       xpathMix.getMixFileName(),
                       xpathTiff.getTifFileName(),
                       xpathAlto.getAltoFileName());

        if (injectedDataSupplied) {
            checkAllEquals(event,
                           FailureType.CROSS_ERROR,
                           "mix metadata (image height: {val1}) tif metadata (image height: {val2}) alto metadata " +
                           "(image " +
                           "height: {val3}) one does not match",
                           xpathTiff.getImageHeightTif(),
                           xpathMix.getMixImageHeight(),
                           xpathAlto.getAltoImageHeight());

            checkAllEquals(event,
                           FailureType.CROSS_ERROR,
                           "mix metadata (image width: {val1}) tif metadata (image width: {val2}) alto metadata " +
                           "(image " +
                           "width: {val3}) one does not match",
                           xpathMix.getMixImageWidth(),
                           xpathTiff.getImageWidthTif(),
                           xpathAlto.getAltoImageWidth());
        }

    }

    private void metsMixAndMixCheck(DecoratedNodeParsingEvent event, XpathMetsMix metsMix, XpathMix mix) {
        checkEquals(event,
                    FailureType.CROSS_ERROR,
                    "Mets mix filename was {actual}, mix filename was {expected}",
                    metsMix.getMixFileName(),
                    mix.getMixFileName());
        checkEquals(event,
                    FailureType.CROSS_ERROR,
                    "Mets mix image height was {actual}, mix image height was {expected}",
                    metsMix.getMixImageHeight(),
                    mix.getMixImageHeight());
        checkEquals(event,
                    FailureType.CROSS_ERROR,
                    "Mets mix image width was {actual}, mix image width was {expected}",
                    metsMix.getMixImageWidth(),
                    mix.getMixImageWidth());
        checkEquals(event,
                    FailureType.CROSS_ERROR,
                    "Mets mix checksum was {actual}, mix checksum was {expected}",
                    metsMix.getChecksumMix(),
                    mix.getChecksumMix());
    }
}
