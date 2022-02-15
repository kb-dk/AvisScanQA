package dk.kb.kula190.iterators.eventhandlers.decorating;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;
import dk.kb.kula190.iterators.common.NodeEndParsingEvent;
import dk.kb.kula190.iterators.common.NodeParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.DefaultTreeEventHandler;
import dk.kb.kula190.iterators.eventhandlers.EventRunner;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    
    void handleNode(NodeParsingEvent event) throws IOException {
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
        } else if (isPage(event)) {
            handlePage(event, lastName);
        }
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
        String[] splits = edition.split("_", 3);
        String avis = splits[0];
        LocalDate editionDate = LocalDate.parse(splits[1], dateFormatter);
        String udgave = splits[2];
        switch (event.getType()) {
            case NodeBegin -> editionBegins(event, avis, editionDate, udgave);
            case NodeEnd -> editionEnds(event, avis, editionDate, udgave);
        }
    }
    
    void handlePage(NodeParsingEvent event, String lastName) throws IOException {
        //modersmaalet_19060706_udg01_0001
        String[] splits = lastName.split("_", 4);
        String avis = splits[0];
        LocalDate editionDate = LocalDate.parse(splits[1], dateFormatter);
        String udgave = splits[2];
        Integer pageNumber = Integer.parseInt(splits[3]);
        switch (event.getType()) {
            case NodeBegin -> pageBegins(event, avis, editionDate, udgave, pageNumber);
            case NodeEnd -> pageEnds(event, avis, editionDate, udgave, pageNumber);
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
    
    void handlePerPageFile(AttributeParsingEvent event) throws IOException {
        //modersmaalet_19060701_udg01_0001.mix.xml
        
        String name = lastName(event.getName());
        String[] splits = name.split("_", 4);
        String avis = splits[0];
        LocalDate editionDate = LocalDate.parse(splits[1], dateFormatter);
        String udgave = splits[2];
        Integer pageNumber = Integer.parseInt(splits[3].split("\\.", 2)[0]);
        if (name.endsWith(".alto") || name.contains(".alto.xml")) {
            altoFile(event, avis, editionDate, udgave, pageNumber);
        } else if (name.endsWith(".mix") || name.endsWith(".mix.xml")) {
            mixFile(event, avis, editionDate, udgave, pageNumber);
        } else if (name.endsWith(".tif")) {
            tiffFile(event, avis, editionDate, udgave, pageNumber);
        } else if (name.endsWith(".pdf")) {
            pdfFile(event, avis, editionDate, udgave, pageNumber);
        }
    }
    
    void handleMetsModsFile(AttributeParsingEvent event) throws IOException {
        //modersmaalet_19060701_19061231_RT1.mets.xml
        
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
    
    
    boolean isEdition(ParsingEvent event) {
        return getLevel(event) == 2 && !Set.of("METS", "MODS").contains(lastName(event.getName())) && event.getName()
                                                                                                           .matches(
                                                                                                                   ".*\\d{8}_udg\\d{2}$");
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
    
    
    boolean isPage(ParsingEvent event) {
        return getLevel(event) == 3;
    }
    
    
    public final int getLevel(ParsingEvent event) {
        return event.getName().split("/").length;
    }
    
    
    public abstract void batchBegins(NodeParsingEvent event,
                                     String avis,
                                     String roundTrip,
                                     LocalDate startDate,
                                     LocalDate endDate) throws IOException;
    
    
    public abstract void batchEnds(NodeParsingEvent event,
                                   String avis,
                                   String roundTrip,
                                   LocalDate startDate,
                                   LocalDate endDate) throws IOException;
    
    
    public abstract void modsBegins(NodeParsingEvent event,
                                    String avis,
                                    String roundTrip,
                                    LocalDate startDate,
                                    LocalDate endDate) throws IOException;
    
    public abstract void modsFile(AttributeParsingEvent event,
                                  String avis,
                                  String roundTrip,
                                  LocalDate startDate,
                                  LocalDate endDate) throws IOException;
    
    public abstract void modsEnds(NodeParsingEvent event,
                                  String avis,
                                  String roundTrip,
                                  LocalDate startDate,
                                  LocalDate endDate) throws IOException;
    
    
    public abstract void metsBegins(NodeParsingEvent event,
                                    String avis,
                                    String roundTrip,
                                    LocalDate startDate,
                                    LocalDate endDate) throws IOException;
    
    
    public abstract void metsFile(AttributeParsingEvent event,
                                  String avis,
                                  String roundTrip,
                                  LocalDate startDate,
                                  LocalDate endDate) throws IOException;
    
    public abstract void metsEnds(NodeParsingEvent event,
                                  String avis,
                                  String roundTrip,
                                  LocalDate startDate,
                                  LocalDate endDate) throws IOException;
    
    
    public abstract void editionBegins(NodeParsingEvent event,
                                       String avis,
                                       LocalDate editionDate,
                                       String editionName) throws IOException;
    
    public abstract void editionEnds(NodeParsingEvent event,
                                     String avis,
                                     LocalDate editionDate,
                                     String editionName) throws IOException;
    
    
    public abstract void pageBegins(NodeParsingEvent event,
                                    String avis,
                                    LocalDate editionDate,
                                    String udgave, Integer pageNumber) throws IOException;
    
    public abstract void pageEnds(NodeParsingEvent event,
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
