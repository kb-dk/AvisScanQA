package dk.kb.kula190.checkers.pagecheckers.xpath;

import dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Document;

import java.io.IOException;
import java.time.LocalDate;

import static dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils.lastName;
import static org.apache.commons.io.FilenameUtils.removeExtension;

public class XpathMix {
    private Integer MixImageHeight;
    private Integer MixImageWidth;
    private String ChecksumMix;
    private String MixFileName;
    private Integer TifSizePerMix;
    private String colorSpace;
    
    public XpathMix() {
    }

    public void setMixXpathData(DecoratedAttributeParsingEvent event,
                                String avis,
                                LocalDate editionDate,
                                String udgave,
                                String sectionName,
                                Integer pageNumber) throws IOException {
        Document document = EventHandlerUtils.handleDocument(event);

        XPathSelector xpath = XpathUtils.createXPathSelector("mix", "http://www.loc.gov/mix/v20");

        String fileName = xpath.selectString(
                document,
                "/mix:mix/mix:BasicDigitalObjectInformation/mix:ObjectIdentifier/mix:objectIdentifierValue");

        MixFileName = removeExtension(lastName(fileName));

        TifSizePerMix = xpath.selectInteger(
                document,
                "/mix:mix/mix:BasicDigitalObjectInformation/mix:fileSize");

        MixImageHeight = xpath.selectInteger(
                document, "/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageHeight");

        MixImageWidth = xpath.selectInteger(
                document,
                "/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageWidth");

        ChecksumMix = xpath.selectString(
                document,
                "/mix:mix/mix:BasicDigitalObjectInformation/mix:Fixity/mix:messageDigest");
        colorSpace = xpath.selectString(document,"/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:PhotometricInterpretation/mix:colorSpace");
    }
/*
<?xml version="1.0" encoding="UTF-8"?>
<mix:mix xsi:schemaLocation="http://www.loc.gov/mix/v20 http://www.loc.gov/standards/mix/mix20/mix20.xsd" xmlns:mix="http://www.loc.gov/mix/v20" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <mix:BasicDigitalObjectInformation>
        <mix:ObjectIdentifier>
            <mix:objectIdentifierType>pathname</mix:objectIdentifierType>
            <mix:objectIdentifierValue>../TIFF/modersmaalet_19060702_udg01_1.sektion_0001.tif</mix:objectIdentifierValue>
        </mix:ObjectIdentifier>
        <mix:fileSize>48646367</mix:fileSize>
        <mix:byteOrder>little endian</mix:byteOrder>
        <mix:Compression>
            <mix:compressionScheme>Uncompressed</mix:compressionScheme>
        </mix:Compression>
        <mix:Fixity>
            <mix:messageDigestAlgorithm>MD5</mix:messageDigestAlgorithm>
            <mix:messageDigest>36153582a86635a84b7b7efd0d42e9a3</mix:messageDigest>
        </mix:Fixity>
    </mix:BasicDigitalObjectInformation>
    <mix:BasicImageInformation>
        <mix:BasicImageCharacteristics>
            <mix:imageWidth>3305</mix:imageWidth>
            <mix:imageHeight>4902</mix:imageHeight>
            <mix:PhotometricInterpretation>
                <mix:colorSpace>RGB</mix:colorSpace>
            </mix:PhotometricInterpretation>
        </mix:BasicImageCharacteristics>
    </mix:BasicImageInformation>
    <mix:ImageCaptureMetadata>
        <mix:SourceInformation>
            <mix:sourceType>Archive</mix:sourceType>
        </mix:SourceInformation>
        <mix:GeneralCaptureInformation>
            <mix:dateTimeCreated>2020-11-05T13:00:19</mix:dateTimeCreated>
            <mix:imageProducer>Ninestars</mix:imageProducer>
        </mix:GeneralCaptureInformation>
        <mix:orientation>normal*</mix:orientation>
    </mix:ImageCaptureMetadata>
    <mix:ImageAssessmentMetadata>
        <mix:SpatialMetrics>
            <mix:samplingFrequencyUnit>in.</mix:samplingFrequencyUnit>
            <mix:xSamplingFrequency>
                <mix:numerator>240</mix:numerator>
                <mix:denominator>1</mix:denominator>
            </mix:xSamplingFrequency>
            <mix:ySamplingFrequency>
                <mix:numerator>240</mix:numerator>
                <mix:denominator>1</mix:denominator>
            </mix:ySamplingFrequency>
        </mix:SpatialMetrics>
        <mix:ImageColorEncoding>
            <mix:BitsPerSample>
                <mix:bitsPerSampleValue>8</mix:bitsPerSampleValue>
                <mix:bitsPerSampleValue>8</mix:bitsPerSampleValue>
                <mix:bitsPerSampleValue>8</mix:bitsPerSampleValue>
                <mix:bitsPerSampleUnit>integer</mix:bitsPerSampleUnit>
            </mix:BitsPerSample>
            <mix:samplesPerPixel>3</mix:samplesPerPixel>
        </mix:ImageColorEncoding>
    </mix:ImageAssessmentMetadata>
</mix:mix>
 
 */
    

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
    
    public String getColorSpace() {
        return colorSpace;
    }
}
