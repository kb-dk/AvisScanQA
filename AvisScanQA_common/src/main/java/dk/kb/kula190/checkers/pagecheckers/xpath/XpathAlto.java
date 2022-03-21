package dk.kb.kula190.checkers.pagecheckers.xpath;

import dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.util.xml.XML;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils.lastName;
import static org.apache.commons.io.FilenameUtils.removeExtension;

public class XpathAlto {
    //test, if it gives more readability in some tests.
    //I don't know where this should be located
    private String AltoFileName;
    private Integer AltoImageHeight;
    private Integer AltoImageWidth;
    public XpathAlto(){
    }

    public void setAltoXpathData(DecoratedAttributeParsingEvent event, String avis, LocalDate editionDate,String udgave,String sectionName,Integer pageNumber) throws IOException {
        Document document = EventHandlerUtils.handleDocument(event);
        XPathSelector xpath = XpathUtils.createXPathSelector("alto", "http://www.loc.gov/standards/alto/ns-v2#");


        String fileName = xpath.selectString(document,
                "/alto:alto/alto:Description/alto:sourceImageInformation/alto:fileName");
        AltoFileName = removeExtension(lastName(fileName));


        //TODO alto: before each in that xpath
        List<String> lines = Arrays.stream(xpath.selectString(document, "/alto:alto/alto:Description/alto:OCRProcessing/alto:ocrProcessingStep/alto:processingStepSettings").split("\n")).toList();

        //Line is "width:2180" | gets -1
        Integer width = Integer.parseInt(lines.stream()
                .filter(line -> line.startsWith("width:"))
                .map(line -> line.split(":", 2)[1].trim())
                .findFirst()
                .orElse("-1"));
        AltoImageWidth = width;
        //Line is "height:2786" | gets -1
        Integer height = Integer.parseInt(lines.stream()
                .filter(line -> line.startsWith("height:"))
                .map(line -> line.split(":", 2)[1].trim())
                .findFirst()
                .orElse("-1"));
        AltoImageHeight = height;
        //ALTO PAGE HEIGHT / (ALTO MEASUREMENT UNIT / DPI) = TIFF HEIGHT

    }


    public String getAltoFileName() {
        return AltoFileName;
    }

    public Integer getAltoImageHeight() {
        return AltoImageHeight;
    }

    public Integer getAltoImageWidth() {
        return AltoImageWidth;
    }
}
