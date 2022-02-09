package dk.kb.kula190.iterators.eventhandlers.decorating;


import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;
import dk.kb.kula190.iterators.common.NodeEndParsingEvent;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Prints the tree to the console. Used for testing purposes.
 */
public class DecoratedConsoleLogger extends DecoratedEventHandler {
    
    private PrintStream out;
    
    public DecoratedConsoleLogger(PrintStream out, ResultCollector resultCollector) {
        super(resultCollector);
        this.out = out;
    }
    
    
    @Override
    public void batchBegins(NodeBeginsParsingEvent event, String batch) {
        out.println(" ".repeat(getLevel(event) * 2) + "<batch name=\"" + batch + "\">");
    }
    
    @Override
    public void batchEnds(NodeEndParsingEvent event, String batch) {
        out.println(" ".repeat(getLevel(event) * 2) + "</batch>");
    }
    
    @Override
    public void editionBegins(NodeBeginsParsingEvent event, String editionName) {
        out.println(" ".repeat(getLevel(event) * 2) + "<edition name=\"" + editionName + "\">");
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
