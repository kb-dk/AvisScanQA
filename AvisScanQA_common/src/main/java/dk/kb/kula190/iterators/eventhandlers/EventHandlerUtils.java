package dk.kb.kula190.iterators.eventhandlers;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import static org.apache.commons.io.FilenameUtils.isExtension;
import static org.apache.commons.io.FilenameUtils.removeExtension;

public class EventHandlerUtils {
    public static final DateTimeFormatter dateFormatter =
            new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4)
                                          .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                                          .appendValue(ChronoField.DAY_OF_MONTH, 2)
                                          .toFormatter();

    public static String lastName(String name) {
        return name.replaceFirst("^(.+?)/([^/]+)$", "$2");
    }

    public static String firstName(String name) {
        return name.replaceFirst("^([^/]+)/.*$", "$1");
    }

    public static String removeXmlExtension(String filename) {
        if (isExtension(filename, "xml")) {
            return removeXmlExtension(removeExtension(filename));
        } else {
            return removeExtension(filename);
        }
    }
}
