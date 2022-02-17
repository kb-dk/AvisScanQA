package dk.kb.kula190.checkers.singlecheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandlerWithSections;
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

public class XpathMixChecker extends DecoratedEventHandlerWithSections {
    public XpathMixChecker(ResultCollector resultCollector) {
        super(resultCollector);
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
        
        
        
        Integer width = xpath.selectInteger(document,
                                            "/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageWidth");
        Integer height = xpath.selectInteger(document,
                                            "/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageHeight");
        //All pages stand up, so height > width.
        //TODO compare
        checkAtLeast(event,
                "INVALID_MIX",
                height.doubleValue(),
                width.doubleValue(),
                "MIX image height: {required} was less than width: {actual}"); //`${height}` not supported in this java version?
        /*
        if(width.compareTo(height) == -1){
            resultCollectorAddFailure("Height is greater than width",event);
        }
        */

        String colorSpace = xpath.selectString(document,"/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:PhotometricInterpretation/mix:colorSpace");
        //TODO Should always be RGB. Compare
        checkEquals(event,
                "INVALID_MIX",
                colorSpace,
                "RGB",
                "MIX colorspace should have been {expected} but was {actual}");
        /*
        if(colorSpace.compareTo("RGB") != 0){
            resultCollectorAddFailure("Colorspace is not RGB its: "+colorSpace,event);
        }
        */
        //TODO? Check on fileSize. But the first filesize doesn't match, incorrect?
        
    }
    /*
    private void resultCollectorAddFailure(String reason,AttributeParsingEvent e){
        getResultCollector().addFailure(e.getName(),
                "Xpath incorrect",
                XpathMixChecker.class.getSimpleName(),
                reason,
                "");
    }
    */
}
