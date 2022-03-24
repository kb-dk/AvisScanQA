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

public class XpathMetsMix {
    private Integer MixImageHeight;
    private Integer MixImageWidth;
    private String ChecksumMix;
    private String MixFileName;
    public XpathMetsMix(){

    }
    public void setMetsMixInjectedFileData(DecoratedAttributeParsingEvent decoratedEvent,
                                           String injectedType,
                                           String avis,
                                           LocalDate editionDate,
                                           String udgave,
                                           String sectionName,
                                           Integer pageNumber) throws IOException {
        Document document = EventHandlerUtils.handleDocument(decoratedEvent);
        XPathSelector xpath = XpathUtils.createXPathSelector("mix", "http://www.loc.gov/mix/v20");

        String fileName = xpath.selectString(
                document,
                "/mix:mix/mix:BasicDigitalObjectInformation/mix:ObjectIdentifier/mix:objectIdentifierValue");

        MixFileName = removeExtension(lastName(fileName));

        MixImageHeight = xpath.selectInteger(
                document, "/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageHeight");

        MixImageWidth = xpath.selectInteger(
                document,
                "/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageWidth");

        ChecksumMix = xpath.selectString(
                document,
                "/mix:mix/mix:BasicDigitalObjectInformation/mix:Fixity/mix:messageDigest");

    }
    
    
    /*
<mix:mix xsi:schemaLocation="http://www.loc.gov/mix/v20 http://www.loc.gov/standards/mix/mix20/mix20.xsd"
         xmlns:mix="http://www.loc.gov/mix/v20">
    <mix:BasicDigitalObjectInformation>
        <mix:ObjectIdentifier>
            <mix:objectIdentifierType>Pathname</mix:objectIdentifierType>
            <mix:objectIdentifierValue>..\TIFF\modersmaalet_19060702_udg01_1.sektion_0001.tif</mix:objectIdentifierValue>
        </mix:ObjectIdentifier>
        <mix:FormatDesignation>
            <mix:formatName>image/tiff</mix:formatName>
        </mix:FormatDesignation>
        <mix:byteOrder>little endian</mix:byteOrder>
        <mix:Compression>
            <mix:compressionScheme>Uncompressed</mix:compressionScheme>
        </mix:Compression>
        <mix:Fixity>
            <mix:messageDigestAlgorithm>MD5</mix:messageDigestAlgorithm>
            <mix:messageDigest>36153582a86635a84b7b7efd0d42e9a3</mix:messageDigest>
        </mix:Fixity>
        <mix:Fixity>
            <mix:messageDigestAlgorithm>CRC32</mix:messageDigestAlgorithm>
            <mix:messageDigest>140ace90</mix:messageDigest>
        </mix:Fixity>
        <mix:Fixity>
            <mix:messageDigestAlgorithm>SHA-1</mix:messageDigestAlgorithm>
            <mix:messageDigest>042821b59937370f67027c1a53a0f48c4d5fd309</mix:messageDigest>
        </mix:Fixity>
    </mix:BasicDigitalObjectInformation>
    <mix:BasicImageInformation>
        <mix:BasicImageCharacteristics>
            <mix:imageWidth>3305</mix:imageWidth>
            <mix:imageHeight>4902</mix:imageHeight>
            <mix:PhotometricInterpretation>
                <mix:colorSpace>RGB</mix:colorSpace>
                <mix:ColorProfile>
                    <mix:IccProfile>
                        <mix:iccProfileName>Adobe RGB (1998)</mix:iccProfileName>
                    </mix:IccProfile>
                </mix:ColorProfile>
                <mix:ReferenceBlackWhite>
                    <mix:Component>
                        <mix:componentPhotometricInterpretation>R</mix:componentPhotometricInterpretation>
                        <mix:footroom>
                            <mix:numerator>0</mix:numerator>
                        </mix:footroom>
                        <mix:headroom>
                            <mix:numerator>255</mix:numerator>
                        </mix:headroom>
                    </mix:Component>
                    <mix:Component>
                        <mix:componentPhotometricInterpretation>G</mix:componentPhotometricInterpretation>
                        <mix:footroom>
                            <mix:numerator>0</mix:numerator>
                        </mix:footroom>
                        <mix:headroom>
                            <mix:numerator>255</mix:numerator>
                        </mix:headroom>
                    </mix:Component>
                    <mix:Component>
                        <mix:componentPhotometricInterpretation>B</mix:componentPhotometricInterpretation>
                        <mix:footroom>
                            <mix:numerator>0</mix:numerator>
                        </mix:footroom>
                        <mix:headroom>
                            <mix:numerator>255</mix:numerator>
                        </mix:headroom>
                    </mix:Component>
                </mix:ReferenceBlackWhite>
            </mix:PhotometricInterpretation>
        </mix:BasicImageCharacteristics>
    </mix:BasicImageInformation>
    <mix:ImageCaptureMetadata>
        <mix:GeneralCaptureInformation>
            <mix:dateTimeCreated>2020-11-05T13:00:19</mix:dateTimeCreated>
        </mix:GeneralCaptureInformation>
        <mix:ScannerCapture>
            <mix:scannerManufacturer>Canon</mix:scannerManufacturer>
            <mix:ScannerModel>
                <mix:scannerModelName>Canon EOS 5DS R</mix:scannerModelName>
            </mix:ScannerModel>
            <mix:ScanningSystemSoftware>
                <mix:scanningSoftwareName>Adobe Photoshop Lightroom Classic 10.0 (Windows)</mix:scanningSoftwareName>
            </mix:ScanningSystemSoftware>
        </mix:ScannerCapture>
        <mix:orientation>normal*</mix:orientation>
    </mix:ImageCaptureMetadata>
    <mix:ImageAssessmentMetadata>
        <mix:SpatialMetrics>
            <mix:samplingFrequencyUnit>in.</mix:samplingFrequencyUnit>
            <mix:xSamplingFrequency>
                <mix:numerator>240</mix:numerator>
            </mix:xSamplingFrequency>
            <mix:ySamplingFrequency>
                <mix:numerator>240</mix:numerator>
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

}

