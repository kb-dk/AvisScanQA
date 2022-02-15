package dk.kb.kula190.checkers.singlecheckers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandlerWithSections;
import dk.kb.util.yaml.YAML;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TiffChecker extends DecoratedEventHandlerWithSections {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    public TiffChecker(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    @Override
    public void tiffFile(AttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        log.debug("Checking {}", event.getLocation());
        
        List<String> lines = runImageMagick(event);
        YAML yaml = parseImageMagickOutput(lines);
        
        //See src/test/resources/sampleImageMagickOutput.yaml for what and how
        
        checkEquals(event,
                    "INVALID_TIFF", yaml.getString("Image.Format"),
                    "TIFF (Tagged Image File Format)",
                    "ImageMagick reports invalid format {actual}. Should have been {expected}");
        
        checkEquals(event,
                    "INVALID_TIFF", yaml.getString("Image.Colorspace"),
                    "sRGB",
                    "ImageMagick reports invalid Colorspace {actual}. Should have been {expected}");
        
        checkEquals(event,
                    "INVALID_TIFF", yaml.getString("Image.Depth"),
                    "8-bit",
                    "ImageMagick reports invalid Bit depth {actual}. Should have been {expected}");
        
        //TODO other tests
    }
    
    
    private YAML parseImageMagickOutput(List<String> lines) throws IOException {
        String json = parseImagemagickOutput(lines);
        String yamlString = json2Yaml(json);
        try (InputStream in = new ReaderInputStream(new StringReader(yamlString), StandardCharsets.UTF_8)) {
            return YAML.parse(in);
        }
    }
    
    private List<String> runImageMagick(AttributeParsingEvent event) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("identify", "-verbose", event.getLocation());
        Process process = builder.start();
        List<String> lines;
        try {
            int returnValue = process.waitFor();
            if (returnValue != 0) {
                throw new IOException("ImageMagick failed with return code "
                                      + returnValue
                                      + " on "
                                      + event.getLocation()
                                      + ". Stdout="
                                      + readStdOut(process)
                                      + "\n\nStdErr="
                                      + readStdErr(process));
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting for ImageMagick on " + event.getLocation(), e);
        }
        lines = readStdOut(process);
        return lines;
    }
    
    private List<String> readStdOut(Process process) throws IOException {
        List<String> lines;
        try (BufferedReader stdOut = process.inputReader(StandardCharsets.UTF_8)) {
            lines = IOUtils.readLines(stdOut);
        }
        return lines;
    }
    
    
    private List<String> readStdErr(Process process) throws IOException {
        List<String> lines;
        try (BufferedReader stdOut = process.errorReader(StandardCharsets.UTF_8)) {
            lines = IOUtils.readLines(stdOut);
        }
        return lines;
    }
    
    public String json2Yaml(String jsonString) throws IOException {
        // parse JSON
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
        JsonNode jsonNodeTree = objectMapper.readTree(jsonString);
        // save it as YAML
        return new YAMLMapper().writeValueAsString(jsonNodeTree);
    }
    
    private String parseImagemagickOutput(List<String> lines) throws IOException {
        /*
Image:
  Filename: /home/abr/Projects/AvisScanQA/data/orig/modersmaalet_19060701_19061231_RT1/TIFF/modersmaalet_19060701_udg01_MODERSMAALETS Søndagsblad_0001.tif
  Format: TIFF (Tagged Image File Format)
  Mime type: image/tiff
  Class: DirectClass
  Geometry: 2201x2767+0+0
  Resolution: 240x240
  Print size: 9.17083x11.5292
  Units: PixelsPerInch
  Colorspace: sRGB
  Type: TrueColor
  Endianness: LSB
  Depth: 8-bit
  Channel depth:
    red: 8-bit
    green: 8-bit
    blue: 8-bit
  Channel statistics:
    Pixels: 6090167
    Red:
      min: 37  (0.145098)
      max: 230 (0.901961)
      mean: 196.108 (0.76905)
      standard deviation: 36.8435 (0.144484)
      kurtosis: 5.22417
      skewness: -2.4058
      entropy: 0.705156
    Green:
      min: 37  (0.145098)
      max: 211 (0.827451)
      mean: 179.28 (0.70306)
      standard deviation: 33.9808 (0.133258)
      kurtosis: 4.61207
      skewness: -2.30003
      entropy: 0.729688
    Blue:
      min: 36  (0.141176)
      max: 185 (0.72549)
      mean: 149.687 (0.58701)
      standard deviation: 27.6728 (0.108521)
      kurtosis: 3.83515
      skewness: -2.13275
      entropy: 0.762016
  Image statistics:
    Overall:
      min: 36  (0.141176)
      max: 230 (0.901961)
      mean: 175.025 (0.686373)
      standard deviation: 32.8324 (0.128754)
      kurtosis: 1.61023
      skewness: -1.3146
      entropy: 0.732287
  Rendering intent: Perceptual
  Gamma: 0.454545
  Chromaticity:
    red primary: (0.64,0.33)
    green primary: (0.3,0.6)
    blue primary: (0.15,0.06)
    white point: (0.3127,0.329)
  Background color: white
  Border color: srgb(223,223,223)
  Matte color: grey74
  Transparent color: black
  Interlace: None
  Intensity: Undefined
  Compose: Over
  Page geometry: 2201x2767+0+0
  Dispose: Undefined
  Iterations: 0
  Compression: None
  Orientation: TopLeft
  Profiles:
    Profile-icc: 560 bytes
    Profile-xmp: 2807 bytes
  Properties:
    date:create: 2022-02-14T15:24:54+00:00
    date:modify: 2021-07-17T16:02:26+00:00
    icc:copyright: Copyright 2000 Adobe Systems Incorporated
    icc:description: Adobe RGB (1998)
    signature: bb21cc6476508e9d71e46f6b40313faaa76a858d674ee7311dc56cde511ad026
    tiff:alpha: unspecified
    tiff:DateTime: 2020-11-05T13:00:08
    tiff:document: modersmaalet_19060701_udg01_MODERSMAALETS Søndagsblad_0001.tif
    tiff:endian: lsb
    tiff:make: Canon
    tiff:model: Canon EOS 5DS R
    tiff:photometric: RGB
    tiff:rows-per-strip: 1
    tiff:software: Adobe Photoshop Lightroom Classic 10.0 (Windows)
    tiff:timestamp: 2020:11:05 13:00:08
  Artifacts:
    filename: /home/abr/Projects/AvisScanQA/data/orig/modersmaalet_19060701_19061231_RT1/TIFF/modersmaalet_19060701_udg01_MODERSMAALETS Søndagsblad_0001.tif
    verbose: true
  Tainted: False
  Filesize: 17.4489MiB
  Number pixels: 6.09017M
  Pixels per second: 185.97MB
  User time: 0.030u
  Elapsed time: 0:01.032
  Version: ImageMagick 6.9.12-37 Q16 x86_64 2022-01-29 https://imagemagick.org
         */
        
        //    https://gist.github.com/kamermans/0c5759fdaaa9fb3eeaaccf1e9a962b9d
        
        StringBuilder result = new StringBuilder();
        int prevDepth = 0;
        Pattern lineMatch = Pattern.compile("^( *)\\b(.+?):(:? (.+)|\\Z)$");
        result.append("{");
        for (String line : lines) {
            line = line.stripTrailing();
            if (line.isEmpty()) {
                continue;
            }
            Matcher matcher = lineMatch.matcher(line);
            if (!matcher.matches()) {
                throw new IOException("Failed to parse line "+line+" from ImageMagick");
            }
            String indent = matcher.group(1);
            String key = matcher.group(2).trim();
            String value = matcher.group(3).trim();
            
            int current_depth = indent.length() / 2;
            
            
            int i = current_depth;
            while (i < prevDepth) {
                //step out
                result.append("},\n");
                i += 1;
            }
            prevDepth = current_depth;
            
            if (value.isEmpty()) { //is this good enough?
                result.append("\"").append(key).append("\":{");
            } else {
                result.append("\"").append(key).append("\":\"").append(value).append("\",");
            }
            result.append("\n");
            
            
        }
        result.append("}".repeat(prevDepth + 1));
        
        return result.toString();
        
    }
}
