package dk.kb.kula190.iterators.eventhandlers.decorating;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;
import dk.kb.kula190.iterators.common.NodeEndParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.DefaultTreeEventHandler;
import dk.kb.kula190.iterators.eventhandlers.EventRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class DecoratedEventHandler extends DefaultTreeEventHandler {
    
    private int level = 0;
    private String batch;
    private String edition;
    private Integer page;
    
    private final ResultCollector resultCollector;
    
    protected DecoratedEventHandler(ResultCollector resultCollector) {this.resultCollector = resultCollector;}
    
    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        if (batch == null) {
            batch = lastName(event.getName());
            batchBegins(event, batch);
        } else if (isMETS(event)) {
            metsBegins(event);
        } else if (isMODS(event)) {
            modsBegins(event);
        } else if (isEdition(event)) {
            edition = lastName(event.getName());
            editionBegins(new EditionBegins(event.getName(), event.getLocation()), edition);
        } else if (isPage(event)) {
            page = pageNumber(event.getName());
            pageBegins(new PageBegins(event.getName(), event.getLocation()), edition, page);
        }
        level += 1;
    }
    
    private Integer pageNumber(String name) {
        return Integer.parseInt(name.replaceFirst("^(.+?)_(\\d+)$", "$2"));
    }
    
    
    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        level -= 1;
        if (batch.equals(lastName(event.getName()))) {
            batchEnds(event, batch);
        } else if (isMETS(event)) {
            metsEnds(event);
        } else if (isMODS(event)) {
            modsEnds(event);
        } else if (isEdition(event)) {
            editionEnds(new EditionEnds(event.getName(), event.getLocation()), edition);
            edition = null;
        } else if (isPage(event)) {
            pageEnds(new PageEnds(event.getName(), event.getLocation()), edition, page);
            page = null;
        }
    }
    
    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        try {
            if (event.getName().endsWith(".alto.xml")) {
                altoFile(event, edition, page);
            } else if (event.getName().endsWith(".mix.xml")) {
                mixFile(event, edition, page);
            } else if (event.getName().endsWith(".tif")) {
                tiffFile(event, edition, page);
            } else if (event.getName().endsWith(".pdf")) {
                pdfFile(event, edition, page);
            } else if (event.getName().endsWith(".mets.xml")) {
                metsFile(event);
            } else if (event.getName().endsWith(".mods.xml")) {
                modsFile(event);
            } else {
                resultCollector.addFailure(event.getName(),
                                           "Unknown Filetype",
                                           this.getClass().getSimpleName(),
                                           "Encounted unexpected file");
            }
        } catch (IOException e) {
            resultCollector.addFailure(event.getName(),
                                       EventRunner.EXCEPTION,
                                       this.getClass().getSimpleName(),
                                       EventRunner.UNEXPECTED_ERROR + e,
                                       Arrays.stream(e.getStackTrace())
                                             .map(StackTraceElement::toString)
                                             .collect(Collectors.joining("\n")));
        }
    }
    
    
    private boolean isEdition(ParsingEvent event) {
        return level == 1 && !Set.of("METS", "MODS").contains(lastName(event.getName()));
    }
    
    private boolean isMETS(ParsingEvent event) {
        return level == 1 && Objects.equals("METS", lastName(event.getName()));
    }
    
    private boolean isMODS(ParsingEvent event) {
        return level == 1 && Objects.equals("MODS", lastName(event.getName()));
    }
    
    private String lastName(String name) {
        return name.replaceFirst("^(.+?)/([^/]+)$", "$2");
    }
    
    private boolean isPage(ParsingEvent event) {
        return level == 2;
    }
    
    public String getBatch() {
        return batch;
    }
    
    public String getEdition() {
        return edition;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void batchBegins(NodeBeginsParsingEvent event, String batch) {
    
    }
    
    public void batchEnds(NodeEndParsingEvent event, String batch) {
    
    }
    
    public void modsBegins(NodeBeginsParsingEvent event) {
    
    }
    
    public void modsEnds(NodeEndParsingEvent event) {
    
    }
    
    public void metsBegins(NodeBeginsParsingEvent event) {
    
    }
    
    public void metsEnds(NodeEndParsingEvent event) {
    
    }
    
    
    public void editionBegins(EditionBegins event, String editionName) {
    }
    
    public void editionEnds(EditionEnds event, String editionName) {
    }
    
    public void pageBegins(PageBegins event, String editionName, Integer pageNumber) {
    }
    
    public void pageEnds(PageEnds event, String editionName, Integer pageNumber) {
    }
    
    public void mixFile(AttributeParsingEvent event, String editionName, Integer pageNumber) throws IOException {
    }
    
    public void tiffFile(AttributeParsingEvent event, String editionName, Integer pageNumber) throws IOException {
    }
    
    public void altoFile(AttributeParsingEvent event, String editionName, Integer pageNumber) throws IOException {
    }
    
    public void pdfFile(AttributeParsingEvent event, String editionName, Integer pageNumber) throws IOException {
    }
    
    public void metsFile(AttributeParsingEvent event) throws IOException {
    }
    
    public void modsFile(AttributeParsingEvent event) throws IOException {
    }
    
    public ResultCollector getResultCollector() {
        return resultCollector;
    }
}
