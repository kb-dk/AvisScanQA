package dk.kb.kula190.checkers.crosscheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.checkers.singlecheckers.TiffChecker;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandlerWithSections;
import dk.kb.util.xml.XML;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XpathCrossChecker extends DecoratedEventHandlerWithSections {
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
    private ThreadLocal<String> TifFileName = new ThreadLocal<>();
    private ThreadLocal<String> TifFileNameMix = new ThreadLocal<>();


    
    @Override
    public void pageBegins(NodeParsingEvent event,
                           String avis,
                           LocalDate editionDate,
                           String udgave,
                           String sectionName,
                           Integer pageNumber) throws IOException {
        //clear the state
        TifSizePerMix.set(null);
        TifSizeActual.set(null);
        TifFileName.set(null);
        TifFileNameMix.set(null);
        
    }
    
    @Override
    public void mixFile(AttributeParsingEvent event,
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
        String tiffIdentifier = xpath.selectString(document,"/mix:mix/mix:BasicDigitalObjectInformation/mix:ObjectIdentifier/mix:objectIdentifierValue");
        String[] tiffIdentifierSplitted = tiffIdentifier.split("/|[.]{1,3}");
        TifFileNameMix.set(tiffIdentifierSplitted[3]);
        //TODO extract other params from mix file
        
        //* filesize correct
        //* checksum
        //* height vs width?
    }
    
    @Override
    public void tiffFile(AttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        TifSizeActual.set((int) new File(event.getLocation()).length());
        //TODO extract properties of the tif file, here, such as checksum and filesize
        //TifFileName.set(event.getName());
        TifFileName.set(regexOnLocation(Pattern.compile("/TIFF/(.*).tif"),event.getLocation()));
        //String temp = regexOnLocation(Pattern.compile("/TIFF/(.*).tif"),event.getLocation());
    }
    
    @Override
    public void pageEnds(NodeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        if (!Objects.equals(TifSizeActual.get(), TifSizePerMix.get())){
            Integer what = TifSizeActual.get();
            Integer what2 = TifSizePerMix.get();
            getResultCollector().addFailure(event.getName(), "MIX_TIFF_CROSSCHECK", this.getClass().getSimpleName(), "mix metadata does not match actual tif file", "tif reports size "+TifSizeActual, "mix reports size "+TifSizePerMix);
        }
        Integer what = TifSizeActual.get();
        Integer what2 = TifSizePerMix.get();
        //TODO here you compare extracted properties from the tif file with values from the mix file
        /*
        if(!Objects.equals(TifFileName.get(),TifFileNameMix.get())){
            String temp1 =TifFileName.get();
            String temp2 =TifFileNameMix.get();
            getResultCollector().addFailure(event.getName(), "MIX_TIFF_CROSSCHECK", this.getClass().getSimpleName(), "mix metadata does not match actual tif file", "tif reports filename "+TifFileName, "mix reports filename "+TifFileNameMix);
        }
        */
    }
    private String regexOnLocation(Pattern p, String text){
        Matcher m = p.matcher(text);
        if(m.find()){
            return m.group(1);
        }
        return "error";
    }
}
