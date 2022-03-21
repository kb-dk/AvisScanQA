package dk.kb.kula190.checkers.filecheckers.tiff;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.InjectingTreeEventHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TiffAnalyzerExiv2 extends InjectingTreeEventHandler {
    public static final String INJECTED_TYPE = "Exiv2 Metadata Properties";
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    public TiffAnalyzerExiv2(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    @Override
    public void handleAttribute(AttributeParsingEvent event) throws IOException {
        if (FilenameUtils.isExtension(event.getName(), "tif")) {
            log.info("Analyzing {} with ImageMagick", event.getLocation());
            
            List<String> lines = runTool(event);
            String propertiesString = toProperties(lines);
            // final String yamlString = toYAML(lines);
            
            pushEvent(event, INJECTED_TYPE,
                      propertiesString.getBytes(StandardCharsets.UTF_8));
            //See src/test/resources/sampleImageMagickOutput.yaml for what and how
        }
        
    }
    
    private String toProperties(List<String> lines) throws IOException {
        String formatted = lines.stream()
                                      .map(line -> line.replaceFirst("\\s+", "="))
                .map(line -> {
                    String[] splits = line.split("=", 2);
                    splits[0] = splits[0].replaceAll(":", ".");
                    return String.join("=", splits);
                })
                .collect(Collectors.joining("\n"));
    
        Properties result= new Properties();
        try (Reader reader = new StringReader(formatted)){
            result.load(reader);
        }
        try (StringWriter writer = new StringWriter()) {
            result.store(writer,null);
            return writer.toString();
        }
    }
    
    
    private List<String> runTool(AttributeParsingEvent event) throws IOException {
        
        ProcessBuilder builder = new ProcessBuilder("exiv2","-u", "-PEIXnt", event.getLocation());
        Process process = builder.start();
        List<String> lines;
        try {
            int returnValue = process.waitFor();
            if (returnValue != 0) {
                throw new IOException("exiv2 failed with return code "
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
    
}
