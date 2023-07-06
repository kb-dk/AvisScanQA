package dk.kb.kula190.checkers.pagecheckers.xpath;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.generated.FailureType;
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
    private String physical_img_nr;
    private ResultCollector resultCollector;

    public XpathAlto(ResultCollector resultCollector) {
        this.resultCollector = resultCollector;

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

        try {
            accuracy = Double.parseDouble(pageNode.getAttributes().getNamedItem("ACCURACY").getNodeValue());
        } catch (Exception e) {
            resultCollector.addFailure(event,
                                       FailureType.INVALID_ALTO_ERROR,
                                       this.getClass().toString(),
                                       "Page node in alto, did not contain ACCURACY");
        }


        try {
            quality = pageNode.getAttributes().getNamedItem("QUALITY").getNodeValue();
        } catch (Exception e) {
            resultCollector.addFailure(event,
                                       FailureType.INVALID_ALTO_ERROR,
                                       this.getClass().toString(),
                                       "Page node in alto, did not contain QUALITY");
        }


        try {
            pageHeight = Double.parseDouble(pageNode.getAttributes().getNamedItem("HEIGHT").getNodeValue());
        } catch (Exception e) {
            resultCollector.addFailure(event,
                                       FailureType.INVALID_ALTO_ERROR,
                                       this.getClass().toString(),
                                       "Page node in alto, did not contain HEIGHT");
        }


        try {
            pageWidth = Double.parseDouble(pageNode.getAttributes().getNamedItem("WIDTH").getNodeValue());
        } catch (Exception e) {
            resultCollector.addFailure(event,
                                       FailureType.INVALID_ALTO_ERROR,
                                       this.getClass().toString(),
                                       "Page node in alto, did not contain WIDTH");
        }

        //Checks page ID is corresponding with filename.
        try {
            pageID = pageNode.getAttributes().getNamedItem("ID").getNodeValue();
        } catch (Exception e) {
            resultCollector.addFailure(event,
                                       FailureType.INVALID_ALTO_ERROR,
                                       this.getClass().toString(),
                                       "Page node in alto, did not contain ID");
        }

        try {
            physical_img_nr = pageNode.getAttributes().getNamedItem("PHYSICAL_IMG_NR").getNodeValue();
        }catch (Exception e) {
            resultCollector.addFailure(event,
                                       FailureType.INVALID_ALTO_ERROR,
                                       this.getClass().toString(),
                                       "Page node in alto, did not contain PHYSICAL_IMG_NR");
        }


        //TODO alto: before each in that xpath
        try{
            processingStepSettings = Arrays.stream(xpath.selectString(document,
                                                                      "/alto:alto/alto:Description/alto:OCRProcessing" +
                                                                      "/alto:ocrProcessingStep/alto:processingStepSettings")
                                                        .split("\n")).toList();
        }catch (Exception e) {
            resultCollector.addFailure(event,
                                       FailureType.INVALID_ALTO_ERROR,
                                       this.getClass().toString(),
                                       "Alto file does not contain processingStepSettings");
        }


        //Line is "width:2180" | gets -1
        try{
            imageWidth = Integer.parseInt(processingStepSettings.stream()
                                                                .filter(line1 -> line1.startsWith("width:"))
                                                                .map(line1 -> line1.split(":", 2)[1].trim())
                                                                .findFirst()
                                                                .orElse("-1"));
        }catch (Exception e) {
            resultCollector.addFailure(event,
                                       FailureType.INVALID_ALTO_ERROR,
                                       this.getClass().toString(),
                                       "processingStepSettings in alto file does not contain width");
        }

        //Line is "height:2786" | gets -1
        try{
            imageHeight = Integer.parseInt(processingStepSettings.stream()
                                                                 .filter(line -> line.startsWith("height:"))
                                                                 .map(line -> line.split(":", 2)[1].trim())
                                                                 .findFirst()
                                                                 .orElse("-1"));
        }catch (Exception e) {
            resultCollector.addFailure(event,
                                       FailureType.INVALID_ALTO_ERROR,
                                       this.getClass().toString(),
                                       "processingStepSettings in alto file does not contain height");
        }

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

    public String getPhysical_img_nr() {
        return physical_img_nr;
    }

    public String getQuality() {
        return quality;
    }

    public List<String> getProcessingStepSettings() {
        return processingStepSettings;
    }
}
