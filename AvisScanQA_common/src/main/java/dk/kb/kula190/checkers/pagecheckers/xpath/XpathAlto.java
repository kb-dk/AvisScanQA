package dk.kb.kula190.checkers.pagecheckers.xpath;

import dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils.lastName;
import static org.apache.commons.io.FilenameUtils.removeExtension;

public class XpathAlto {
    //test, if it gives more readability in some tests.
    //I don't know where this should be located
    private String fileName;
    private Integer imageHeight;
    private Integer imageWidth;
    
    private double accuracy;
    private double pageHeight;
    private double pageWidth;
    private String pageID;
    private String quality;
    private List<String> processingStepSettings;
    
    
    public XpathAlto() {
    }
    
    public void setAltoXpathData(DecoratedAttributeParsingEvent event,
                                 String avis,
                                 LocalDate editionDate,
                                 String udgave,
                                 String sectionName,
                                 Integer pageNumber) throws IOException {
        Document document = EventHandlerUtils.handleDocument(event);
        XPathSelector xpath = XpathUtils.createXPathSelector("alto", "http://www.loc.gov/standards/alto/ns-v2#");
        
        
        String fileName = xpath.selectString(document,
                                             "/alto:alto/alto:Description/alto:sourceImageInformation/alto:fileName");
        this.fileName = removeExtension(lastName(fileName));
        
        
        Node pageNode = xpath.selectNode(document, "/alto:alto/alto:Layout/alto:Page");
    
    
        accuracy = Double.parseDouble(pageNode.getAttributes().getNamedItem("ACCURACY").getNodeValue());
    
    
        quality = pageNode.getAttributes().getNamedItem("QUALITY").getNodeValue();
        
        
        pageHeight = Double.parseDouble(pageNode.getAttributes().getNamedItem("HEIGHT").getNodeValue());
        
        
        pageWidth = Double.parseDouble(pageNode.getAttributes().getNamedItem("WIDTH").getNodeValue());
        //Checks page ID is corresponding with filename.
    
        pageID = pageNode.getAttributes().getNamedItem("ID").getNodeValue();
        
        
        //TODO alto: before each in that xpath
        processingStepSettings = Arrays.stream(xpath.selectString(document,
                                                              "/alto:alto/alto:Description/alto:OCRProcessing/alto:ocrProcessingStep/alto:processingStepSettings")
                                                .split("\n")).toList();
        
        //Line is "width:2180" | gets -1
        imageWidth = Integer.parseInt(processingStepSettings.stream()
                                           .filter(line1 -> line1.startsWith("width:"))
                                           .map(line1 -> line1.split(":", 2)[1].trim())
                                           .findFirst()
                                           .orElse("-1"));
        //Line is "height:2786" | gets -1
        imageHeight = Integer.parseInt(processingStepSettings.stream()
                                            .filter(line -> line.startsWith("height:"))
                                            .map(line -> line.split(":", 2)[1].trim())
                                            .findFirst()
                                            .orElse("-1"));
        //ALTO PAGE HEIGHT / (ALTO MEASUREMENT UNIT / DPI) = TIFF HEIGHT
        
    }
    
    
    public String getFileName() {
        return fileName;
    }
    
    public Integer getImageHeight() {
        return imageHeight;
    }
    
    public Integer getImageWidth() {
        return imageWidth;
    }
    
    public double getAccuracy() {
        return accuracy;
    }
    
    public double getPageHeight() {
        return pageHeight;
    }
    
    public double getPageWidth() {
        return pageWidth;
    }
    
    public String getPageID() {
        return pageID;
    }
    
    public String getQuality() {
        return quality;
    }
    
    public List<String> getProcessingStepSettings() {
        return processingStepSettings;
    }
}
