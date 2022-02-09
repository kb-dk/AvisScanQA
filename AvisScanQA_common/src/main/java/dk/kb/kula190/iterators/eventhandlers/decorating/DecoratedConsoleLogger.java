package dk.kb.kula190.iterators.eventhandlers.decorating;


import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;

import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * Prints the tree to the console. Used for testing purposes.
 */
public class DecoratedConsoleLogger extends DecoratedEventHandler {
    
    private PrintStream out;
    
    private static final DateTimeFormatter dateFormatter =
            new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4)
                                          .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                                          .appendValue(ChronoField.DAY_OF_MONTH, 2)
                                          .toFormatter();
    
    public DecoratedConsoleLogger(PrintStream out, ResultCollector resultCollector) {
        super(resultCollector);
        this.out = out;
    }
    
    
    private void indent(ParsingEvent event) {
        int level = getLevel(event) - 1;
        String string = "  ".repeat(level);
        out.print(string);
    }
    
    @Override
    public void modsFile(AttributeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {
        indent(event);
        out.println("<modsFile checksum=\"" + event.getChecksum() + "\"/>");
    }
    
    
    @Override
    public void batchBegins(ParsingEvent event,
                            String avis,
                            String roundTrip,
                            LocalDate startDate,
                            LocalDate endDate) {
        indent(event);
        out.println("<batch "
                    + "avis=\"" + avis + "\" "
                    + "start=\"" + startDate + "\" "
                    + "end=\"" + endDate + "\" "
                    + "roundtrip=\"" + roundTrip + "\" "
                    + ">");
    }
    
    @Override
    public void batchEnds(ParsingEvent event, String batch, String roundTrip, LocalDate startDate, LocalDate endDate) {
        indent(event);
        out.println("</batch>");
    }
    
    @Override
    public void modsBegins(ParsingEvent event, String avis, String roundTrip, LocalDate startDate, LocalDate endDate) {
        indent(event);
        out.println("<mods>");
    }
    
    @Override
    public void modsEnds(ParsingEvent event, String avis, String roundTrip, LocalDate startDate, LocalDate endDate) {
        indent(event);
        out.println("</mods>");
    }
    
    @Override
    public void metsBegins(ParsingEvent event, String avis, String roundTrip, LocalDate startDate, LocalDate endDate) {
        indent(event);
        out.println("<mets>");
    }
    
    @Override
    public void metsEnds(ParsingEvent event, String avis, String roundTrip, LocalDate startDate, LocalDate endDate) {
        indent(event);
        out.println("</mets>");
    }
    
    @Override
    public void editionBegins(ParsingEvent event, String avis, LocalDate editionDate, String editionName) {
        indent(event);
        out.println("<edition "
                    + "date=\"" + editionDate + "\" "
                    + "udgave=\"" + editionName + "\" "
                    + ">");
    }
    
    @Override
    public void editionEnds(ParsingEvent event, String avis, LocalDate editionDate, String editionName) {
        indent(event);
        out.println("</edition>");
    }
    
    @Override
    public void sectionBegins(ParsingEvent event,
                              String editionName,
                              LocalDate editionDate,
                              String udgave,
                              String section) {
        indent(event);
        out.println("<section "
                    + "section=\"" + section + "\" "
                    + ">");
    }
    
    @Override
    public void sectionEnds(ParsingEvent event,
                            String editionName,
                            LocalDate editionDate,
                            String udgave,
                            String section) {
        indent(event);
        out.println("</section>");
    }
    
    @Override
    public void pageBegins(ParsingEvent event,
                           String editionName,
                           LocalDate editionDate,
                           String udgave,
                           String sectionName,
                           Integer pageNumber) {
        indent(event);
        out.println("<page number=\"" + pageNumber + "\">");
    }
    
    @Override
    public void pageEnds(ParsingEvent event,
                         String editionName,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) {
        indent(event);
        out.println("</page>");
    }
    
    
    @Override
    public void metsFile(AttributeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {
        indent(event);
        out.println("<metsFile checksum=\"" + event.getChecksum() + "\"/>");
    }
    
    
    @Override
    public void mixFile(AttributeParsingEvent event,
                        String editionName,
                        LocalDate editionDate,
                        String udgave,
                        String sectionName,
                        Integer pageNumber) throws IOException {
        indent(event);
        out.println("<mixFile checksum=\"" + event.getChecksum() + "\"/>");
    }
    
    @Override
    public void tiffFile(AttributeParsingEvent event,
                         String editionName,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        indent(event);
        out.println("<tiffFile checksum=\"" + event.getChecksum() + "\"/>");
    }
    
    @Override
    public void altoFile(AttributeParsingEvent event,
                         String editionName,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        indent(event);
        out.println("<altoFile checksum=\"" + event.getChecksum() + "\"/>");
    }
    
    @Override
    public void pdfFile(AttributeParsingEvent event,
                        String editionName,
                        LocalDate editionDate,
                        String udgave,
                        String sectionName,
                        Integer pageNumber) throws IOException {
        indent(event);
        out.println("<pdfFile checksum=\"" + event.getChecksum() + "\"/>");
    }
    
}
