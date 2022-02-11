package dk.kb.kula190.iterators.eventhandlers.decorating;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;

import java.io.IOException;
import java.time.LocalDate;

public abstract class AbstractDecoratedEventHandlerWithSections extends AbstractDecoratedEventHandler {
    
    
    public AbstractDecoratedEventHandlerWithSections(ResultCollector resultCollector) {
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
    
    boolean isSection(ParsingEvent event) {
        return getLevel(event) == 3;
    }
    
    @Override
    boolean isPage(ParsingEvent event) {
        return getLevel(event) == 4;
    }
    
    void handleSection(NodeParsingEvent event, String section) throws IOException {
        //modersmaalet_19060706_udg01_1.sektion
        String[] splits = section.split("_", 4);
        String avis = splits[0];
        LocalDate editionDate = LocalDate.parse(splits[1], dateFormatter);
        String udgave = splits[2];
        String sectionName = splits[3];
        switch (event.getType()) {
            case NodeBegin -> sectionBegins(event, avis, editionDate, udgave, sectionName);
            case NodeEnd -> sectionEnds(event, avis, editionDate, udgave, sectionName);
        }
    }
    
    @Override
    void handlePage(NodeParsingEvent event, String lastName) throws IOException {
        //modersmaalet_19060706_udg01_1.sektion_0001
        String[] splits = lastName.split("_", 5);
        String avis = splits[0];
        LocalDate editionDate = LocalDate.parse(splits[1], dateFormatter);
        String udgave = splits[2];
        String sectionName = splits[3];
        Integer pageNumber = Integer.parseInt(splits[4]);
        switch (event.getType()) {
            case NodeBegin -> pageBegins(event, avis, editionDate, udgave, sectionName, pageNumber);
            case NodeEnd -> pageEnds(event, avis, editionDate, udgave, sectionName, pageNumber);
        }
    }
    
    @Override
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
    
    @Override
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
    
    
    public abstract void sectionBegins(NodeParsingEvent event,
                                       String avis,
                                       LocalDate editionDate,
                                       String udgave, String section) throws IOException;
    
    public abstract void sectionEnds(NodeParsingEvent event,
                                     String avis,
                                     LocalDate editionDate,
                                     String udgave, String section) throws IOException;
    
    
    public abstract void pageBegins(NodeParsingEvent event,
                                    String avis,
                                    LocalDate editionDate,
                                    String udgave, String sectionName, Integer pageNumber) throws IOException;
    
    public abstract void pageEnds(NodeParsingEvent event,
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
    
}
