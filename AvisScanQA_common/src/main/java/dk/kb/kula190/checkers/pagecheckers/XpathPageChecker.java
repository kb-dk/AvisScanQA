package dk.kb.kula190.checkers.pagecheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.checkers.batchcheckers.MetsSplitter;
import dk.kb.kula190.checkers.filecheckers.tiff.TiffAnalyzerImageMagick;
import dk.kb.kula190.checkers.pagecheckers.xpath.XpathAlto;
import dk.kb.kula190.checkers.pagecheckers.xpath.XpathMetsMix;
import dk.kb.kula190.checkers.pagecheckers.xpath.XpathMix;
import dk.kb.kula190.checkers.pagecheckers.xpath.XpathTiff;
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
public class XpathPageChecker extends DecoratedEventHandler {
    public XpathPageChecker(ResultCollector resultCollector) {
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
    //private ThreadLocal<XpathMix> Mix = new ThreadLocal<>();
    private ThreadLocal<XpathTiff> Tiff = new ThreadLocal<>();
    private ThreadLocal<XpathMetsMix> MetsMix = new ThreadLocal<>();

    
    @Override
    public void pageBegins(DecoratedNodeParsingEvent event,
                           String newspaper,
                           LocalDate editionDate,
                           String udgave,
                           String section,
                           Integer pageNumber) throws IOException {
        
        //clear state
        //Mix.set(new XpathMix());
        Alto.set(new XpathAlto(getResultCollector()));
        Tiff.set(new XpathTiff());
        MetsMix.set(new XpathMetsMix());
    }
    /*
    @Override
    public void mixFile(DecoratedAttributeParsingEvent event,
                        String newspaper,
                        LocalDate editionDate,
                        String edition,
                        String section,
                        Integer pageNumber) throws IOException {
        //object
        XpathMix xpathMix = Mix.get();
        xpathMix.setMixXpathData(event, newspaper, editionDate, edition, section, pageNumber);
        
        //Perform MIX-only checks here
        
        checkEquals(event,
                    FailureType.INVALID_MIX_ERROR,
                    "Appendix E – metadata per page MIX: MIX colorspace should have been {expected} but was {actual}",
                    xpathMix.getColorSpace(),
                    "RGB"
                   );
    
    }
    */
    @Override
    public void tiffFile(DecoratedAttributeParsingEvent event,
                         String newspaper,
                         LocalDate editionDate,
                         String edition,
                         String section,
                         Integer pageNumber) throws IOException {
        //object
        XpathTiff xpathTiff = Tiff.get();
        xpathTiff.setTiffXpathData(event, newspaper, editionDate, edition, section, pageNumber);
    
        //Perform TIFF-only checks here
    }
    
    @Override
    public void altoFile(DecoratedAttributeParsingEvent event, String newspaper, LocalDate editionDate, String edition,
                         String section, Integer pageNumber) throws IOException {
        
        XpathAlto xpathAlto = Alto.get();
        xpathAlto.setAltoXpathData(event, newspaper, editionDate, edition, section, pageNumber);
        
        
        //TODO better checks of actual values https://sbprojects.statsbiblioteket.dk/jira/browse/IOF-33
        checkEquals(event,
                    FailureType.INVALID_ALTO_ERROR,
                    "Appendix F – metadata per page ALTO: ALTO quality should have been {expected} but was {actual}",
                    xpathAlto.getQuality(),
                    "OK"
                   );
        
        
        //Checks page ID is corresponding with filename.
        checkEquals(event,
                    FailureType.INVALID_ALTO_ERROR,
                    "Appendix F – metadata per page ALTO: ALTO Page ID is not {expected} but was {actual}",
                    xpathAlto.getPageID(),
                    "P" + pageNumber
                   );
    }
    
 
    
    @Override
    public void injectedFile(DecoratedAttributeParsingEvent decoratedEvent, String injectedType, String newspaper,
                             LocalDate editionDate, String edition, String section, Integer pageNumber)
            throws IOException {
        
        switch (injectedType) {
            case TiffAnalyzerImageMagick.INJECTED_TYPE -> { // ImageMagick Output
                XpathTiff xpathTiff = Tiff.get();
                xpathTiff.setTiffInjectedFileData(decoratedEvent, injectedType, newspaper, editionDate, edition, section,
                                                  pageNumber);
            }
            case MetsSplitter.INJECTED_TYPE_MIX -> { //MIX FROM METS FILE
                //        This is the mix extracted from METS for a specific page
                XpathMetsMix xpathMetsMix = MetsMix.get();
                xpathMetsMix.setMetsMixInjectedFileData(decoratedEvent,
                                                        injectedType,
                                                        newspaper,
                                                        editionDate,
                                                        edition,
                                                        section,
                                                        pageNumber);
            }
        }
    }
    
    @Override
    public void pageEnds(DecoratedNodeParsingEvent event,
                         String newspaper,
                         LocalDate editionDate,
                         String edition,
                         String section,
                         Integer pageNumber) {
        
        //XpathMix xpathMix = Mix.get();
        XpathTiff xpathTiff = Tiff.get();
        XpathAlto xpathAlto = Alto.get();
        //XpathMetsMix xpathMetsMix = MetsMix.get();

        //metsMixAndMixCheck(event, xpathMetsMix, xpathMix);

        boolean injectedDataSupplied = xpathTiff.isInjectedDataSupplied();
        
        checkTrue(event,
                  FailureType.TIFF_ANALYZE_ERROR,
                  "Imagemagick metadata for tiff not supplied",
                  injectedDataSupplied);
        if (injectedDataSupplied) {
            
            
            //checkTiffMix(event, xpathMix, xpathTiff);
    
            //checkTiffMixAlto(event, xpathMix, xpathTiff, xpathAlto);
        }
        
    }
    
    private void checkTiffMixAlto(DecoratedNodeParsingEvent event,
                           XpathMix xpathMix,
                           XpathTiff xpathTiff,
                           XpathAlto xpathAlto) {
        checkAllEquals(event,
                       FailureType.INCONSISTENCY_ERROR,
                       "MIX/TIFF/ALTO: Mix metadata (filename: {0}), tif metadata (filename: {1}), alto metadata (filename: {2}). One is not like the others",
                       xpathMix.getMixFileName(),
                       xpathTiff.getTifFileName(),
                       xpathAlto.getFileName());
        
        checkAllEquals(event,
                       FailureType.INCONSISTENCY_ERROR,
                       "MIX/TIFF/ALTO: Mix metadata (image height: {0}), tif metadata (image height: {1}), alto metadata "
                       +
                       "(image height: {2}). One is not like the others",
                       xpathTiff.getImageHeightTif(),
                       xpathMix.getMixImageHeight(),
                       xpathAlto.getImageHeight());
        
        checkAllEquals(event,
                       FailureType.INCONSISTENCY_ERROR,
                       "MIX/TIFF/ALTO: Mix metadata (image width: {0}), tif metadata (image width: {1}), alto metadata " +
                       "(image " +
                       "width: {2}). One is not like the others",
                       xpathMix.getMixImageWidth(),
                       xpathTiff.getImageWidthTif(),
                       xpathAlto.getImageWidth());
    }
    
    private void checkTiffMix(DecoratedNodeParsingEvent event, XpathMix xpathMix, XpathTiff xpathTiff) {
        checkEquals(event,
                    FailureType.INCONSISTENCY_ERROR,
                    "MIX/TIFF: Mix metadata (file size: {expected}) does not match actual tif file size {actual}",
                    xpathTiff.getTifSizeActual(),
                    xpathMix.getTifSizePerMix());
        
        checkEquals(event,
                    FailureType.INCONSISTENCY_ERROR,
                    "MIX/TIFF: metadata (checksum {expected}) does not match actual tif file checksum {actual}",
                    xpathTiff.getChecksumTif(),
                    xpathMix.getChecksumMix());
    }
    
    private void metsMixAndMixCheck(DecoratedNodeParsingEvent event, XpathMetsMix metsMix, XpathMix mix) {
        checkEquals(event,
                    FailureType.INCONSISTENCY_ERROR,
                    "MetsMIX/MIX: Mets mix filename was {actual}, mix filename was {expected}",
                    metsMix.getMixFileName(),
                    mix.getMixFileName());
        checkEquals(event,
                    FailureType.INCONSISTENCY_ERROR,
                    "MetsMIX/MIX: Mets mix image height was {actual}, mix image height was {expected}",
                    metsMix.getMixImageHeight(),
                    mix.getMixImageHeight());
        checkEquals(event,
                    FailureType.INCONSISTENCY_ERROR,
                    "MetsMIX/MIX: Mets mix image width was {actual}, mix image width was {expected}",
                    metsMix.getMixImageWidth(),
                    mix.getMixImageWidth());
        checkEquals(event,
                    FailureType.INCONSISTENCY_ERROR,
                    "MetsMIX/MIX: Mets mix checksum was {actual}, mix checksum was {expected}",
                    metsMix.getChecksumMix(),
                    mix.getChecksumMix());
    }
}
