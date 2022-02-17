package dk.kb.kula190.checkers.crosscheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
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
import java.util.Objects;

public class XpathCrossChecker extends DecoratedEventHandler {
    public XpathCrossChecker(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    
    //The design:
    //A page consist of (at least) a tiff and a mix file
    
    //On page begin, we clear the state
    //We will then get a mixFileEvent and a tiffFileEvent. We do NOT know the order of these
    //When we have the relevant file, we extract the interesting properties
    //On page end, we KNOW we have visited both files
    //It is here we compare values between them
    
    
    //part of the state. This is the size of the tif file, as reported by mix
    private ThreadLocal<Integer> TifSizePerMix = new ThreadLocal<>();
    private ThreadLocal<Integer> TifSizeActual = new ThreadLocal<>();
    
    
    @Override
    public void pageBegins(DecoratedNodeParsingEvent event,
                           String avis,
                           LocalDate editionDate,
                           String udgave,
                           String sectionName,
                           Integer pageNumber) throws IOException {
        //clear the state
        TifSizePerMix.set(null);
        TifSizeActual.set(null);
        
    }
    
    @Override
    public void mixFile(DecoratedAttributeParsingEvent event,
                        String avis,
                        LocalDate editionDate,
                        String udgave,
                        String sectionName,
                        Integer pageNumber) throws IOException {
        Document document;
        try (InputStream in = event.getData()) {
            document = XML.fromXML(in, true);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new IOException(e);
        }
        XPathSelector xpath = XpathUtils.createXPathSelector("mix", "http://www.loc.gov/mix/v20");
        
        Integer value = xpath.selectInteger(document,
                                            "/mix:mix/mix:BasicDigitalObjectInformation/mix:fileSize");
        TifSizePerMix.set(value);
        
        //TODO compare this tif file name with this file name (event.getName) to see if some mapping is messed up
        
        //TODO extract other params from mix file
        
        //* filesize correct
        //* checksum
        //* height vs width?
    }
    
    @Override
    public void tiffFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        TifSizeActual.set((int) new File(event.getLocation()).length());
        //TODO extract properties of the tif file, here, such as checksum and filesize
    }
    
    @Override
    public void pageEnds(DecoratedNodeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        if (!Objects.equals(TifSizeActual.get(), TifSizePerMix.get())){
            getResultCollector().addFailure(event.getName(), "MIX_TIFF_CROSSCHECK", this.getClass().getSimpleName(), "mix metadata does not match actual tif file", "tif reports size "+TifSizeActual, "mix reports size "+TifSizePerMix);
        }
        //TODO here you compare extracted properties from the tif file with values from the mix file
    }
}
