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

public abstract class AbstractDecoratedEventHandler extends DefaultTreeEventHandler {
    
    protected static final DateTimeFormatter dateFormatter =
            new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4)
                                          .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                                          .appendValue(ChronoField.DAY_OF_MONTH, 2)
                                          .toFormatter();
    protected final InheritableThreadLocal<String> batchName = new InheritableThreadLocal<>();
    protected final InheritableThreadLocal<String> batchLocation = new InheritableThreadLocal<>();
    
    public AbstractDecoratedEventHandler(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    @Override
    public final void handleNode(NodeParsingEvent event) throws IOException {
        String lastName = lastName(event.getName());
        if (batchName.get() == null) {
            //modersmaalet_19060701_19061231_RT1
            batchName.set(lastName);
            batchLocation.set(event.getLocation());
            handleBatch(event, lastName);
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
            case NodeBegin -> sectionBegins(decoratedEvent);
            case NodeEnd -> sectionEnds(decoratedEvent);
        }
    }
    
    private DecoratedNodeParsingEvent createDecoratedParsingEvent(NodeParsingEvent event) {
        //modersmaalet_19060706_udg01_1.sektion
        String[] splits = lastName(event.getName()).split("_", 5);
        String avis = splits[0];
        LocalDate editionDate = LocalDate.parse(splits[1], dateFormatter);
        String udgave = splits[2];
        String sectionName = null;
        if (splits.length > 3) {
            sectionName = splits[3];
        }
        Integer pageNumber = null;
        if (splits.length > 4){
            pageNumber = Integer.parseInt(splits[4]);
        }
        
        String[] splits2 = batchName.get().split("_", 4);
        LocalDate startDate = LocalDate.parse(splits2[1], dateFormatter);
        LocalDate endDate = LocalDate.parse(splits2[2], dateFormatter);
        String avis2 = splits[0];
        String roundTrip = splits[3].replaceFirst("^RT", "");
    
        
        DecoratedNodeParsingEvent decoratedEvent = new DecoratedNodeParsingEvent(event.getName(),
                                                                                 event.getType(),
                                                                                 event.getLocation(),
                                                                                 avis,
                                                                                 roundTrip,
                                                                                 startDate,
                                                                                 endDate,
                                                                                 editionDate,
                                                                                 udgave,
                                                                                 sectionName,
                                                                                 pageNumber);
        return decoratedEvent;
    }
    
    
    @Override
    public final void handleFinish() throws IOException {
        handleBatch(new NodeEndParsingEvent(batchName.get(), batchLocation.get()), batchName.get());
    }
    
    @Override
    public final void handleNodeBegin(NodeBeginsParsingEvent event) throws IOException {
        handleNode(event);
    }
    
    @Override
    public final void handleNodeEnd(NodeEndParsingEvent event) throws IOException {
        handleNode(event);
    }
    
    void handleBatch(NodeParsingEvent event, String lastName) throws IOException {
        //modersmaalet_19060701_19061231_RT1
        String[] splits = lastName.split("_", 4);
        LocalDate startDate = LocalDate.parse(splits[1], dateFormatter);
        LocalDate endDate = LocalDate.parse(splits[2], dateFormatter);
        String avis = splits[0];
        String roundTrip = splits[3].replaceFirst("^RT", "");
        switch (event.getType()) {
            case NodeBegin -> batchBegins(event, avis, roundTrip, startDate, endDate);
            case NodeEnd -> batchEnds(event, avis, roundTrip, startDate, endDate);
        }
    }
    
    void handleMets(NodeParsingEvent event, String batchID) throws IOException {
        //modersmaalet_19060701_19061231_RT1
        String[] splits = batchID.split("_", 4);
        LocalDate startDate = LocalDate.parse(splits[1], dateFormatter);
        LocalDate endDate = LocalDate.parse(splits[2], dateFormatter);
        String avis = splits[0];
        String roundTrip = splits[3].replaceFirst("^RT", "");
        switch (event.getType()) {
            case NodeBegin -> metsBegins((NodeBeginsParsingEvent) event, avis, roundTrip, startDate, endDate);
            case NodeEnd -> metsEnds((NodeEndParsingEvent) event, avis, roundTrip, startDate, endDate);
        }
    }
    
    void handleMods(NodeParsingEvent event, String batchID) throws IOException {
        //modersmaalet_19060701_19061231_RT1
        String[] splits = batchID.split("_", 4);
        LocalDate startDate = LocalDate.parse(splits[1], dateFormatter);
        LocalDate endDate = LocalDate.parse(splits[2], dateFormatter);
        String avis = splits[0];
        String roundTrip = splits[3].replaceFirst("^RT", "");
        switch (event.getType()) {
            case NodeBegin -> modsBegins(event, avis, roundTrip, startDate, endDate);
            case NodeEnd -> modsEnds(event, avis, roundTrip, startDate, endDate);
        }
    }
    
    void handleEdition(NodeParsingEvent event, String edition) throws IOException {
        //modersmaalet_19060706_udg01
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
            reportException(event,e);
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
    
    String lastName(String name) {
        return name.replaceFirst("^(.+?)/([^/]+)$", "$2");
    }
    
    
    public final int getLevel(ParsingEvent event) {
        return event.getName().split("/").length;
    }
    
    void handlePage(NodeParsingEvent event, String lastName) throws IOException {
        //modersmaalet_19060706_udg01_1.sektion_0001
        DecoratedNodeParsingEvent
                decoratedEvent
                = createDecoratedParsingEvent(event);
        switch (event.getType()) {
            case NodeBegin -> pageBegins(decoratedEvent, decoratedEvent.getAvis(), decoratedEvent.getEditionDate(), decoratedEvent.getUdgave(), decoratedEvent.getSectionName(), decoratedEvent.getPageNumber());
            case NodeEnd -> pageEnds(decoratedEvent, decoratedEvent.getAvis(), decoratedEvent.getEditionDate(), decoratedEvent.getUdgave(), decoratedEvent.getSectionName(), decoratedEvent.getPageNumber());
        }
    }
    
    void handlePerPageFile(AttributeParsingEvent event) throws IOException {
        //modersmaalet_19060701_udg01_MODERSMAALETS Søndagsblad_0001.mix.xml
        
        String name = lastName(event.getName());
        String[] splits = name.split("_", 5);
        String avis = splits[0];
        LocalDate editionDate = LocalDate.parse(splits[1], dateFormatter);
        String udgave = splits[2];
        String sectionName = splits[3];
        Integer pageNumber = Integer.parseInt(splits[4].split("\\.", 2)[0]);
        if (name.endsWith(".alto") || name.contains(".alto.xml")) {
            altoFile(event, avis, editionDate, udgave, sectionName, pageNumber);
        } else if (name.endsWith(".mix") || name.endsWith(".mix.xml")) {
            mixFile(event, avis, editionDate, udgave, sectionName, pageNumber);
        } else if (name.endsWith(".tif")) {
            tiffFile(event, avis, editionDate, udgave, sectionName, pageNumber);
        } else if (name.endsWith(".pdf")) {
            pdfFile(event, avis, editionDate, udgave, sectionName, pageNumber);
        }
    }
    
    void handleMetsModsFile(AttributeParsingEvent event) throws IOException {
        String name = lastName(event.getName());
        String[] splits = batchName.get().split("_", 4);
        LocalDate startDate = LocalDate.parse(splits[1], dateFormatter);
        LocalDate endDate = LocalDate.parse(splits[2], dateFormatter);
        String avis = splits[0];
        String roundTrip = splits[3].replaceFirst("^RT", "");
        if (name.endsWith(".mets") || name.endsWith(".mets.xml")) {
            metsFile(event, avis, roundTrip, startDate, endDate);
        } else if (name.endsWith(".mods") || name.endsWith(".mods.xml")) {
            modsFile(event, avis, roundTrip, startDate, endDate);
        }
    }
    
    
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
    
    
    public abstract void mixFile(AttributeParsingEvent event,
                                 String avis,
                                 LocalDate editionDate,
                                 String udgave, String sectionName, Integer pageNumber) throws IOException;
    
    public abstract void tiffFile(AttributeParsingEvent event,
                                  String avis,
                                  LocalDate editionDate,
                                  String udgave, String sectionName, Integer pageNumber) throws IOException;
    
    public abstract void altoFile(AttributeParsingEvent event,
                                  String avis,
                                  LocalDate editionDate,
                                  String udgave, String sectionName, Integer pageNumber) throws IOException;
    
    public abstract void pdfFile(AttributeParsingEvent event,
                                 String avis,
                                 LocalDate editionDate,
                                 String udgave, String sectionName, Integer pageNumber) throws IOException;
    
    
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
    
    public abstract void modsFile(AttributeParsingEvent event,
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
    
    
    public abstract void metsFile(AttributeParsingEvent event,
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
    
    
    public abstract void pageBegins(DecoratedNodeParsingEvent event,
                                    String avis,
                                    LocalDate editionDate,
                                    String udgave, Integer pageNumber) throws IOException;
    
    public abstract void pageEnds(DecoratedNodeParsingEvent event,
                                  String avis,
                                  LocalDate editionDate,
                                  String udgave, Integer pageNumber) throws IOException;
    
    
    public abstract void mixFile(AttributeParsingEvent event,
                                 String avis,
                                 LocalDate editionDate,
                                 String udgave, Integer pageNumber) throws IOException;
    
    public abstract void tiffFile(AttributeParsingEvent event,
                                  String avis,
                                  LocalDate editionDate,
                                  String udgave, Integer pageNumber) throws IOException;
    
    public abstract void altoFile(AttributeParsingEvent event,
                                  String avis,
                                  LocalDate editionDate,
                                  String udgave, Integer pageNumber) throws IOException;
    
    public abstract void pdfFile(AttributeParsingEvent event,
                                 String avis,
                                 LocalDate editionDate,
                                 String udgave, Integer pageNumber) throws IOException;
    
    
    
}
