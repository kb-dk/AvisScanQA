package dk.kb.kula190.iterators.eventhandlers.decorating;


import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;
import dk.kb.kula190.iterators.common.NodeEndParsingEvent;

import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

/**
 * Prints the tree to the console. Used for testing purposes.
 */
public class DecoratedConsoleLogger extends DecoratedEventHandler {
    
    private PrintStream out;
    
    private static final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR,
                                                                                                      4)
                                                                                         .appendValue(ChronoField.MONTH_OF_YEAR,
                                                                                                      2)
                                                                                         .appendValue(ChronoField.DAY_OF_MONTH,
                                                                                                      2)
                                                                                         .toFormatter();
    
    public DecoratedConsoleLogger(PrintStream out, ResultCollector resultCollector) {
        super(resultCollector);
        this.out = out;
    }
    
    
    @Override
    public void batchBegins(NodeBeginsParsingEvent event, String batch) {
        //modersmaalet_19060701_19061231_RT1
        String[] batchSplits = batch.split("_", 4);
        TemporalAccessor startDate = LocalDate.parse(batchSplits[1], dateFormatter);
        TemporalAccessor endDate = LocalDate.parse(batchSplits[2], dateFormatter);
        String avis = batchSplits[0];
        String roundTrip = batchSplits[3].replaceFirst("^RT", "");
        
        out.println(" ".repeat(getLevel(event) * 2) + "<batch "
                    + "avis=\"" + avis + "\" "
                    + "start=\"" + startDate + "\" "
                    + "end=\"" + endDate + "\" "
                    + "roundtrip=\"" + roundTrip + "\" "
                    //+ "name=\"" + batch + "\""
                    + ">");
    }
    
    @Override
    public void batchEnds(NodeEndParsingEvent event, String batch) {
        out.println(" ".repeat(getLevel(event) * 2) + "</batch>");
    }
    
    @Override
    public void editionBegins(NodeBeginsParsingEvent event, String editionName) {
        //modersmaalet_19060706_udg01_1.sektion
        String[] editionNameSplits = editionName.split("_", 4);
        LocalDate editionDate = LocalDate.parse(editionNameSplits[1], dateFormatter);
        String udgave = editionNameSplits[2];
        String section = editionNameSplits[3];
        out.println(" ".repeat(getLevel(event) * 2)
                    + "<edition "
                    + "date=\"" + editionDate + "\" "
                    + "udgave=\"" + udgave + "\" "
                    + "section=\"" + section + "\" "
                    + "name=\"" + editionName + "\""
                    + ">");
    }
    
    @Override
    public void editionEnds(NodeEndParsingEvent event, String editionName) {
        out.println(" ".repeat(getLevel(event) * 2) + "</edition>");
    }
    
    @Override
    public void pageBegins(NodeBeginsParsingEvent event, String editionName, Integer pageNumber) {
        out.println(" ".repeat(getLevel(event) * 2) + "<page number=\"" + pageNumber + "\">");
    }
    
    @Override
    public void pageEnds(NodeEndParsingEvent event, String editionName, Integer pageNumber) {
        out.println(" ".repeat(getLevel(event) * 2) + "</page>");
    }
    
    @Override
    public void mixFile(AttributeParsingEvent event, String editionName, Integer pageNumber) throws IOException {
        out.println(" ".repeat(getLevel(event) * 2) + "<mixFile checksum=\"" + event.getChecksum() + "\"/>");
    }
    
    @Override
    public void tiffFile(AttributeParsingEvent event, String editionName, Integer pageNumber) throws IOException {
        out.println(" ".repeat(getLevel(event) * 2) + "<tiffFile checksum=\"" + event.getChecksum() + "\"/>");
    }
    
    @Override
    public void altoFile(AttributeParsingEvent event, String editionName, Integer pageNumber) throws IOException {
        out.println(" ".repeat(getLevel(event) * 2) + "<altoFile checksum=\"" + event.getChecksum() + "\"/>");
    }
    
    @Override
    public void pdfFile(AttributeParsingEvent event, String editionName, Integer pageNumber) throws IOException {
        out.println(" ".repeat(getLevel(event) * 2) + "<pdfFile checksum=\"" + event.getChecksum() + "\"/>");
    }
    
    @Override
    public void modsBegins(NodeBeginsParsingEvent event) {
        out.println(" ".repeat(getLevel(event) * 2) + "<mods>");
    }
    
    @Override
    public void modsEnds(NodeEndParsingEvent event) {
        out.println(" ".repeat(getLevel(event) * 2) + "</mods>");
    }
    
    @Override
    public void metsBegins(NodeBeginsParsingEvent event) {
        out.println(" ".repeat(getLevel(event) * 2) + "<mets>");
    }
    
    @Override
    public void metsEnds(NodeEndParsingEvent event) {
        out.println(" ".repeat(getLevel(event) * 2) + "</mets>");
    }
    
    @Override
    public void metsFile(AttributeParsingEvent event) throws IOException {
        out.println(" ".repeat(getLevel(event) * 2) + "<metsFile checksum=\"" + event.getChecksum() + "\"/>");
    }
    
    @Override
    public void modsFile(AttributeParsingEvent event) throws IOException {
        out.println(" ".repeat(getLevel(event) * 2) + "<modsFile checksum=\"" + event.getChecksum() + "\"/>");
    }
}
