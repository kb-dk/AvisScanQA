package dk.kb.kula190.checkers.filecheckers.tiff;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.Utils;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.InjectingTreeEventHandler;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
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
            log.trace("Analyzing {} with exiv2", event.getLocation());

            List<String> lines = Utils.runTool("exiv2", event.getLocation(), "-u", "-PEIXnt");
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

        Properties result = new Properties();
        try (Reader reader = new StringReader(formatted)) {
            result.load(reader);
        }
        try (StringWriter writer = new StringWriter()) {
            result.store(writer, null);
            return writer.toString();
        }
    }


}
