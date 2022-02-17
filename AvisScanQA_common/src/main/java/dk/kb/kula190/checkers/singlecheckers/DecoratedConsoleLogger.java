package dk.kb.kula190.checkers.singlecheckers;


import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedNodeParsingEvent;

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
    
    private static final DateTimeFormatter dateFormatter =
            new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4)
                                          .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                                          .appendValue(ChronoField.DAY_OF_MONTH, 2)
                                          .toFormatter();
    private PrintStream out;
    
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
    public void modsFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {
        indent(event);
        out.println("<modsFile checksum=\"" + event.getChecksum() + "\"/>");
    }
    
    
    @Override
    public void batchBegins(DecoratedNodeParsingEvent event,
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
    public void batchEnds(DecoratedNodeParsingEvent event,
                          String avis,
                          String roundTrip,
                          LocalDate startDate,
                          LocalDate endDate) {
        indent(event);
        out.println("</batch>");
    }
    
    @Override
    public void modsBegins(DecoratedNodeParsingEvent event,
                           String avis,
                           String roundTrip,
                           LocalDate startDate,
                           LocalDate endDate) {
        indent(event);
        out.println("<mods>");
    }
    
    @Override
    public void modsEnds(DecoratedNodeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) {
        indent(event);
        out.println("</mods>");
    }
    
    @Override
    public void metsBegins(DecoratedNodeParsingEvent event,
                           String avis,
                           String roundTrip,
                           LocalDate startDate,
                           LocalDate endDate) {
        indent(event);
        out.println("<mets>");
    }
    
    @Override
    public void metsEnds(DecoratedNodeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) {
        indent(event);
        out.println("</mets>");
    }
    
    @Override
    public void editionBegins(DecoratedNodeParsingEvent event, String avis, LocalDate editionDate, String editionName) {
        indent(event);
        out.println("<edition "
                    + "date=\"" + editionDate + "\" "
                    + "udgave=\"" + editionName + "\" "
                    + ">");
    }
    
    @Override
    public void editionEnds(DecoratedNodeParsingEvent event, String avis, LocalDate editionDate, String editionName) {
        indent(event);
        out.println("</edition>");
    }
    
    @Override
    public void sectionBegins(DecoratedNodeParsingEvent event,
                              String avis,
                              LocalDate editionDate,
                              String udgave,
                              String section) {
        indent(event);
        out.println("<section "
                    + "section=\"" + section + "\" "
                    + ">");
    }
    
    @Override
    public void sectionEnds(DecoratedNodeParsingEvent event,
                            String avis,
                            LocalDate editionDate,
                            String udgave,
                            String section) {
        indent(event);
        out.println("</section>");
    }
    
    @Override
    public void pageBegins(DecoratedNodeParsingEvent event,
                           String editionName,
                           LocalDate editionDate,
                           String udgave,
                           String sectionName,
                           Integer pageNumber) {
        indent(event);
        out.println("<page number=\"" + pageNumber + "\">");
    }
    
    @Override
    public void pageEnds(DecoratedNodeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) {
        indent(event);
        out.println("</page>");
    }
    
    
    @Override
    public void metsFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {
        indent(event);
        out.println("<metsFile checksum=\"" + event.getChecksum() + "\"/>");
    }
    
    
    @Override
    public void mixFile(DecoratedAttributeParsingEvent event,
                        String avis,
                        LocalDate editionDate,
                        String udgave,
                        String sectionName,
                        Integer pageNumber) throws IOException {
        indent(event);
        out.println("<mixFile checksum=\"" + event.getChecksum() + "\"/>");
    }
    
    @Override
    public void tiffFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        indent(event);
        out.println("<tiffFile checksum=\"" + event.getChecksum() + "\"/>");
    }
    
    @Override
    public void altoFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        indent(event);
        out.println("<altoFile checksum=\"" + event.getChecksum() + "\"/>");
    }
    
    @Override
    public void pdfFile(DecoratedAttributeParsingEvent event,
                        String avis,
                        LocalDate editionDate,
                        String udgave,
                        String sectionName,
                        Integer pageNumber) throws IOException {
        indent(event);
        out.println("<pdfFile checksum=\"" + event.getChecksum() + "\"/>");
    }
    
}
