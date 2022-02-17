package dk.kb.kula190.iterators.eventhandlers.decorating;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;
import dk.kb.kula190.iterators.common.NodeEndParsingEvent;
import dk.kb.kula190.iterators.common.NodeParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.DefaultTreeEventHandler;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Objects;
import java.util.Set;

import static org.apache.commons.io.FilenameUtils.isExtension;
import static org.apache.commons.io.FilenameUtils.removeExtension;

public abstract class AbstractDecoratedEventHandler extends DefaultTreeEventHandler {
    
    static final DateTimeFormatter dateFormatter =
            new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4)
                                          .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                                          .appendValue(ChronoField.DAY_OF_MONTH, 2)
                                          .toFormatter();
    protected final InheritableThreadLocal<String> batchName = new InheritableThreadLocal<>();
    protected final InheritableThreadLocal<String> batchLocation = new InheritableThreadLocal<>();
    
    public AbstractDecoratedEventHandler(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    public final void handleNode(NodeParsingEvent event) throws IOException {
        String lastName = lastName(event.getName());
        if (batchName.get() == null) {
            //modersmaalet_19060701_19061231_RT1
            batchName.set(lastName);
            batchLocation.set(event.getLocation());
            handleBatch(event);
        } else if (isMETS(event)) {
            this.handleMets(event, batchName.get());
        } else if (isMODS(event)) {
            handleMods(event, batchName.get());
        } else if (isEdition(event)) {
            handleEdition(event, lastName);
        } else if (isSection(event)) {
            handleSection(event, lastName);
        } else if (isPage(event)) {
            handlePage(event, lastName);
        }
    }
    
    
    void handleSection(NodeParsingEvent event, String section) throws IOException {
        DecoratedNodeParsingEvent
                decoratedEvent
                = createDecoratedParsingEvent(event);
        switch (event.getType()) {
            case NodeBegin -> sectionBegins(decoratedEvent,
                                            decoratedEvent.getAvis(),
                                            decoratedEvent.getEditionDate(),
                                            decoratedEvent.getUdgave(),
                                            decoratedEvent.getSectionName());
            case NodeEnd -> sectionEnds(decoratedEvent,
                                        decoratedEvent.getAvis(),
                                        decoratedEvent.getEditionDate(),
                                        decoratedEvent.getUdgave(),
                                        decoratedEvent.getSectionName());
        }
    }
    
    private DecoratedNodeParsingEvent createDecoratedParsingEvent(NodeParsingEvent event) {
        DecoratedNodeParsingEvent decoratedEvent = new DecoratedNodeParsingEvent(event);
        return decoratedEvent;
    }
    
    private DecoratedAttributeParsingEvent createDecoratedParsingEvent(AttributeParsingEvent event) {
        return new DecoratedAttributeParsingEvent(event);
    }
    
    
    
    @Override
    public final void handleFinish() throws IOException {
        handleBatch(new NodeEndParsingEvent(batchName.get(), batchLocation.get()));
    }
    
    @Override
    public final void handleNodeBegin(NodeBeginsParsingEvent event) throws IOException {
        handleNode(event);
    }
    
    @Override
    public final void handleNodeEnd(NodeEndParsingEvent event) throws IOException {
        handleNode(event);
    }
    
    void handleBatch(NodeParsingEvent event) throws IOException {
        DecoratedNodeParsingEvent
                decoratedEvent
                = createDecoratedParsingEvent(event);
        switch (event.getType()) {
            case NodeBegin -> batchBegins(decoratedEvent,
                                          decoratedEvent.getAvis(),
                                          decoratedEvent.getRoundTrip(),
                                          decoratedEvent.getStartDate(),
                                          decoratedEvent.getEndDate());
            case NodeEnd -> batchEnds(decoratedEvent,
                                      decoratedEvent.getAvis(),
                                      decoratedEvent.getRoundTrip(),
                                      decoratedEvent.getStartDate(),
                                      decoratedEvent.getEndDate());
        }
    }
    
    void handleMets(NodeParsingEvent event, String batchID) throws IOException {
        DecoratedNodeParsingEvent
                decoratedEvent
                = createDecoratedParsingEvent(event);
        switch (event.getType()) {
            case NodeBegin -> metsBegins(decoratedEvent,
                                         decoratedEvent.getAvis(),
                                         decoratedEvent.getRoundTrip(),
                                         decoratedEvent.getStartDate(),
                                         decoratedEvent.getEndDate());
            case NodeEnd -> metsEnds(decoratedEvent,
                                     decoratedEvent.getAvis(),
                                     decoratedEvent.getRoundTrip(),
                                     decoratedEvent.getStartDate(),
                                     decoratedEvent.getEndDate());
        }
    }
    
    void handleMods(NodeParsingEvent event, String batchID) throws IOException {
        DecoratedNodeParsingEvent
                decoratedEvent
                = createDecoratedParsingEvent(event);
        switch (event.getType()) {
            case NodeBegin -> modsBegins(decoratedEvent,
                                         decoratedEvent.getAvis(),
                                         decoratedEvent.getRoundTrip(),
                                         decoratedEvent.getStartDate(),
                                         decoratedEvent.getEndDate());
            case NodeEnd -> modsEnds(decoratedEvent,
                                     decoratedEvent.getAvis(),
                                     decoratedEvent.getRoundTrip(),
                                     decoratedEvent.getStartDate(),
                                     decoratedEvent.getEndDate());
        }
    }
    
    void handleEdition(NodeParsingEvent event, String edition) throws IOException {
        DecoratedNodeParsingEvent
                decoratedEvent
                = createDecoratedParsingEvent(event);
        switch (event.getType()) {
            case NodeBegin -> editionBegins(decoratedEvent, decoratedEvent.getAvis(), decoratedEvent.getEditionDate(),
                                            decoratedEvent.getUdgave());
            case NodeEnd -> editionEnds(decoratedEvent, decoratedEvent.getAvis(), decoratedEvent.getEditionDate(),
                                        decoratedEvent.getUdgave());
        }
    }
    
    
    @Override
    public final void handleAttribute(AttributeParsingEvent event) {
        try {
            if (event.getName().matches(".*\\.((mets)|(mods))(\\.xml)?$")) {
                handleMetsModsFile(event);
            } else if (event.getName().matches(".*\\.(((mix|alto)(\\.xml)?)|(pdf)|(tiff?))$")) {
                handlePerPageFile(event);
            } else {
                getResultCollector().addFailure(event.getName(),
                                                "Unknown Filetype",
                                                this.getClass().getSimpleName(),
                                                "Encounted unexpected file");
            }
        } catch (IOException e) {
            reportException(event, e);
        }
    }
    
    
    boolean isEdition(ParsingEvent event) {
        return getLevel(event) == 2 && !Set.of("METS", "MODS").contains(lastName(event.getName())) && event.getName()
                                                                                                           .matches(
                                                                                                                   ".*\\d{8}_udg\\d{2}$");
    }
    
    boolean isSection(ParsingEvent event) {
        return getLevel(event) == 3;
    }
    
    boolean isPage(ParsingEvent event) {
        return getLevel(event) == 4;
    }
    
    boolean isMETS(ParsingEvent event) {
        return getLevel(event) == 2 && Objects.equals("METS", lastName(event.getName()));
    }
    
    boolean isMODS(ParsingEvent event) {
        return getLevel(event) == 2 && Objects.equals("MODS", lastName(event.getName()));
    }
    
    static String lastName(String name) {
        return name.replaceFirst("^(.+?)/([^/]+)$", "$2");
    }
    
    static String firstName(String name) {
        return name.replaceFirst("^([^/]+)/.*$", "$1");
    }
    
    public final int getLevel(ParsingEvent event) {
        return event.getName().split("/").length;
    }
    
    void handlePage(NodeParsingEvent event, String lastName) throws IOException {
        DecoratedNodeParsingEvent
                decoratedEvent
                = createDecoratedParsingEvent(event);
        switch (event.getType()) {
            case NodeBegin -> pageBegins(decoratedEvent,
                                         decoratedEvent.getAvis(),
                                         decoratedEvent.getEditionDate(),
                                         decoratedEvent.getUdgave(),
                                         decoratedEvent.getSectionName(),
                                         decoratedEvent.getPageNumber());
            case NodeEnd -> pageEnds(decoratedEvent,
                                     decoratedEvent.getAvis(),
                                     decoratedEvent.getEditionDate(),
                                     decoratedEvent.getUdgave(),
                                     decoratedEvent.getSectionName(),
                                     decoratedEvent.getPageNumber());
        }
    }
    
    void handlePerPageFile(AttributeParsingEvent event) throws IOException {
        DecoratedAttributeParsingEvent
                decoratedEvent
                = createDecoratedParsingEvent(event);
        String name = lastName(event.getName());
        if (name.endsWith(".alto") || name.contains(".alto.xml")) {
            altoFile(decoratedEvent,
                     decoratedEvent.getAvis(),
                     decoratedEvent.getEditionDate(),
                     decoratedEvent.getUdgave(),
                     decoratedEvent.getSectionName(),
                     decoratedEvent.getPageNumber());
        } else if (name.endsWith(".mix") || name.endsWith(".mix.xml")) {
            mixFile(decoratedEvent,
                    decoratedEvent.getAvis(),
                    decoratedEvent.getEditionDate(),
                    decoratedEvent.getUdgave(),
                    decoratedEvent.getSectionName(),
                    decoratedEvent.getPageNumber());
        } else if (name.endsWith(".tif")) {
            tiffFile(decoratedEvent,
                     decoratedEvent.getAvis(),
                     decoratedEvent.getEditionDate(),
                     decoratedEvent.getUdgave(),
                     decoratedEvent.getSectionName(),
                     decoratedEvent.getPageNumber());
        } else if (name.endsWith(".pdf")) {
            pdfFile(decoratedEvent,
                    decoratedEvent.getAvis(),
                    decoratedEvent.getEditionDate(),
                    decoratedEvent.getUdgave(),
                    decoratedEvent.getSectionName(),
                    decoratedEvent.getPageNumber());
        }
    }
    
    void handleMetsModsFile(AttributeParsingEvent event) throws IOException {
        String name = lastName(event.getName());
        
        DecoratedAttributeParsingEvent
                decoratedEvent
                = createDecoratedParsingEvent(event);
        
        if (name.endsWith(".mets") || name.endsWith(".mets.xml")) {
            metsFile(decoratedEvent, decoratedEvent.getAvis(),
                     decoratedEvent.getRoundTrip(), decoratedEvent.getStartDate(), decoratedEvent.getEndDate());
        } else if (name.endsWith(".mods") || name.endsWith(".mods.xml")) {
            modsFile(decoratedEvent, decoratedEvent.getAvis(),
                     decoratedEvent.getRoundTrip(), decoratedEvent.getStartDate(), decoratedEvent.getEndDate());
        }
    }
    
    
    public abstract void batchBegins(DecoratedNodeParsingEvent event,
                                     String avis,
                                     String roundTrip,
                                     LocalDate startDate,
                                     LocalDate endDate) throws IOException;
    
    
    public abstract void batchEnds(DecoratedNodeParsingEvent event,
                                   String avis,
                                   String roundTrip,
                                   LocalDate startDate,
                                   LocalDate endDate) throws IOException;
    
    
    public abstract void modsBegins(DecoratedNodeParsingEvent event,
                                    String avis,
                                    String roundTrip,
                                    LocalDate startDate,
                                    LocalDate endDate) throws IOException;
    
    public abstract void modsFile(DecoratedAttributeParsingEvent event,
                                  String avis,
                                  String roundTrip,
                                  LocalDate startDate,
                                  LocalDate endDate) throws IOException;
    
    public abstract void modsEnds(DecoratedNodeParsingEvent event,
                                  String avis,
                                  String roundTrip,
                                  LocalDate startDate,
                                  LocalDate endDate) throws IOException;
    
    
    public abstract void metsBegins(DecoratedNodeParsingEvent event,
                                    String avis,
                                    String roundTrip,
                                    LocalDate startDate,
                                    LocalDate endDate) throws IOException;
    
    
    public abstract void metsFile(DecoratedAttributeParsingEvent event,
                                  String avis,
                                  String roundTrip,
                                  LocalDate startDate,
                                  LocalDate endDate) throws IOException;
    
    public abstract void metsEnds(DecoratedNodeParsingEvent event,
                                  String avis,
                                  String roundTrip,
                                  LocalDate startDate,
                                  LocalDate endDate) throws IOException;
    
    
    public abstract void editionBegins(DecoratedNodeParsingEvent event,
                                       String avis,
                                       LocalDate editionDate,
                                       String editionName) throws IOException;
    
    public abstract void editionEnds(DecoratedNodeParsingEvent event,
                                     String avis,
                                     LocalDate editionDate,
                                     String editionName) throws IOException;
    
    
    public abstract void sectionBegins(DecoratedNodeParsingEvent event,
                                       String avis,
                                       LocalDate editionDate,
                                       String udgave, String section) throws IOException;
    
    public abstract void sectionEnds(DecoratedNodeParsingEvent event,
                                     String avis,
                                     LocalDate editionDate,
                                     String udgave, String section) throws IOException;
    
    
    public abstract void pageBegins(DecoratedNodeParsingEvent event,
                                    String avis,
                                    LocalDate editionDate,
                                    String udgave, String sectionName, Integer pageNumber) throws IOException;
    
    public abstract void pageEnds(DecoratedNodeParsingEvent event,
                                  String avis,
                                  LocalDate editionDate,
                                  String udgave, String sectionName, Integer pageNumber) throws IOException;
    
    
    public abstract void mixFile(DecoratedAttributeParsingEvent event,
                                 String avis,
                                 LocalDate editionDate,
                                 String udgave, String sectionName, Integer pageNumber) throws IOException;
    
    public abstract void tiffFile(DecoratedAttributeParsingEvent event,
                                  String avis,
                                  LocalDate editionDate,
                                  String udgave, String sectionName, Integer pageNumber) throws IOException;
    
    public abstract void altoFile(DecoratedAttributeParsingEvent event,
                                  String avis,
                                  LocalDate editionDate,
                                  String udgave, String sectionName, Integer pageNumber) throws IOException;
    
    public abstract void pdfFile(DecoratedAttributeParsingEvent event,
                                 String avis,
                                 LocalDate editionDate,
                                 String udgave, String sectionName, Integer pageNumber) throws IOException;
    
    
}
