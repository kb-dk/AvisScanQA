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
    
    private InheritableThreadLocal<String> batch = new InheritableThreadLocal<>();
    private InheritableThreadLocal<String> edition = new InheritableThreadLocal<>();
    private InheritableThreadLocal<Integer> page = new InheritableThreadLocal<>();
    
    private final ResultCollector resultCollector;
    
    protected DecoratedEventHandler(ResultCollector resultCollector) {this.resultCollector = resultCollector;}
    
    @Override
    public final void handleNodeBegin(NodeBeginsParsingEvent event) {
        if (batch.get() == null) {
            batch.set(lastName(event.getName()));
            batchBegins(event, batch.get());
        } else if (isMETS(event)) {
            metsBegins(event);
        } else if (isMODS(event)) {
            modsBegins(event);
        } else if (isEdition(event)) {
            edition.set(lastName(event.getName()));
            editionBegins(event, edition.get());
        } else if (isPage(event)) {
            page.set(pageNumber(event.getName()));
            pageBegins(event, edition.get(), page.get());
        }
    }
    
    private Integer pageNumber(String name) {
        return Integer.parseInt(name.replaceFirst("^(.+?)_(\\d+)$", "$2"));
    }
    
    
    @Override
    public final void handleNodeEnd(NodeEndParsingEvent event) {
        if (batch.get().equals(lastName(event.getName()))) {
            batchEnds(event, batch.get());
        } else if (isMETS(event)) {
            metsEnds(event);
        } else if (isMODS(event)) {
            modsEnds(event);
        } else if (isEdition(event)) {
            editionEnds(event, edition.get());
            edition.set(null);
        } else if (isPage(event)) {
            pageEnds(event, edition.get(), page.get());
            page.set(null);
        }
    }
    
    @Override
    public final void handleAttribute(AttributeParsingEvent event) {
        try {
            if (event.getName().endsWith(".alto.xml")) {
                altoFile(event, edition.get(), page.get());
            } else if (event.getName().endsWith(".mix.xml")) {
                mixFile(event, edition.get(), page.get());
            } else if (event.getName().endsWith(".tif")) {
                tiffFile(event, edition.get(), page.get());
            } else if (event.getName().endsWith(".pdf")) {
                pdfFile(event, edition.get(), page.get());
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
        return getLevel(event) == 2 && !Set.of("METS", "MODS").contains(lastName(event.getName()));
    }
    
    private boolean isMETS(ParsingEvent event) {
        return getLevel(event) == 2 && Objects.equals("METS", lastName(event.getName()));
    }
    
    private boolean isMODS(ParsingEvent event) {
        return getLevel(event) == 2 && Objects.equals("MODS", lastName(event.getName()));
    }
    
    private String lastName(String name) {
        return name.replaceFirst("^(.+?)/([^/]+)$", "$2");
    }
    
    private boolean isPage(ParsingEvent event) {
        return getLevel(event) == 3;
    }
    
    public final String getBatch() {
        return batch.get();
    }
    
    public final String getEdition() {
        return edition.get();
    }
    
    public final Integer getPage() {
        return page.get();
    }
    
    public final int getLevel(ParsingEvent event) {
        return event.getName().split("/").length;
    }
    
    
    public void batchBegins(NodeBeginsParsingEvent event, String batch) {}
    
    public void batchEnds(NodeEndParsingEvent event, String batch) {}
    
    public void modsBegins(NodeBeginsParsingEvent event) {}
    
    public void modsFile(AttributeParsingEvent event) throws IOException {}
    
    public void modsEnds(NodeEndParsingEvent event) {}
    
    public void metsBegins(NodeBeginsParsingEvent event) {}
    
    public void metsFile(AttributeParsingEvent event) throws IOException {}
    
    public void metsEnds(NodeEndParsingEvent event) {}
    
    public void editionBegins(NodeBeginsParsingEvent event, String editionName) {}
    
    public void editionEnds(NodeEndParsingEvent event, String editionName) {}
    
    public void pageBegins(NodeBeginsParsingEvent event, String editionName, Integer pageNumber) {}
    
    public void pageEnds(NodeEndParsingEvent event, String editionName, Integer pageNumber) {}
    
    public void mixFile(AttributeParsingEvent event, String editionName, Integer pageNumber) throws IOException {}
    
    public void tiffFile(AttributeParsingEvent event, String editionName, Integer pageNumber) throws IOException {}
    
    public void altoFile(AttributeParsingEvent event, String editionName, Integer pageNumber) throws IOException {}
    
    public void pdfFile(AttributeParsingEvent event, String editionName, Integer pageNumber) throws IOException {}
    
    
    
    
    
    public ResultCollector getResultCollector() {
        return resultCollector;
    }
}
