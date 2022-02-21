package dk.kb.kula190.checkers.crosscheckers;

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

import static dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils.lastName;
import static org.apache.commons.io.FilenameUtils.removeExtension;

public class XpathMix {
    private Integer MixImageHeight;
    private Integer MixImageWidth;
    private String ChecksumMix;
    private String MixFileName;
    private Integer TifSizePerMix;

    public XpathMix(){
    }
    public void setMixXpathData(DecoratedAttributeParsingEvent event, String avis, LocalDate editionDate, String udgave, String sectionName, Integer pageNumber) throws IOException {
        Document document = handleDocument(event);

        XPathSelector xpath = XpathUtils.createXPathSelector("mix", "http://www.loc.gov/mix/v20");

        String fileName = xpath.selectString(document,
                "/mix:mix/mix:BasicDigitalObjectInformation/mix:ObjectIdentifier/mix:objectIdentifierValue");
        MixFileName = removeExtension(lastName(fileName));

        Integer value = xpath.selectInteger(document,
                "/mix:mix/mix:BasicDigitalObjectInformation/mix:fileSize");
        TifSizePerMix = value;

        Integer mixHeight = xpath.selectInteger(document,"/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageHeight");
        MixImageHeight = mixHeight;
        Integer mixWidth = xpath.selectInteger(document,"/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageWidth");
        MixImageWidth = mixWidth;
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

    public Integer getMixImageHeight() {
        return MixImageHeight;
    }

    public Integer getMixImageWidth() {
        return MixImageWidth;
    }

    public String getChecksumMix() {
        return ChecksumMix;
    }

    public String getMixFileName() {
        return MixFileName;
    }

    public Integer getTifSizePerMix() {
        return TifSizePerMix;
    }
}
