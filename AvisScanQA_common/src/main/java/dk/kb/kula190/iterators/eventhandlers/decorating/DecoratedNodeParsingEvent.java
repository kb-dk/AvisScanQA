package dk.kb.kula190.iterators.eventhandlers.decorating;

import dk.kb.kula190.iterators.common.NodeParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEventType;

import java.time.LocalDate;

public class DecoratedNodeParsingEvent extends NodeParsingEvent {
    
    private String avis;
    private String roundTrip;
    private LocalDate startDate;
    private LocalDate endDate;
    
    private LocalDate editionDate;
    private String udgave;
    private String sectionName;
    private Integer pageNumber;
    
    
    public DecoratedNodeParsingEvent(NodeParsingEvent delegate) {
        super(delegate.getName(), delegate.getType(), delegate.getLocation());
    
    
        final String lastName = AbstractDecoratedEventHandler.lastName(delegate.getName());
    
        String avis = null;
        LocalDate editionDate = null;
        String udgave = null;
        String sectionName = null;
        Integer pageNumber = null;
    
        String batch;
        if (lastName.matches(".*_RT\\d+$")) {
            //batchlike
            //batch: modersmaalet_19060701_19061231_RT1
            //mets/mods:  modersmaalet_19060701_19061231_RT1
            batch = lastName;
        } else { //page/section/edition-like
            batch = AbstractDecoratedEventHandler.firstName(delegate.getName());
    
            //section: modersmaalet_19060706_udg01_1.sektion
            //edition: modersmaalet_19060706_udg01
            //page: modersmaalet_19060706_udg01_1.sektion_001
            String[] splits = lastName.split("_", 5);
            if (splits.length > 1) {
                avis        = splits[0];
                editionDate = LocalDate.parse(splits[1], AbstractDecoratedEventHandler.dateFormatter);
                udgave      = splits[2];
                if (splits.length > 3) {
                    sectionName = splits[3];
                }
                if (splits.length > 4) {
                    pageNumber = Integer.parseInt(splits[4]);
                }
            }
        }
    
    
        String[] splits2 = batch.split("_", 4);
        LocalDate startDate = LocalDate.parse(splits2[1], AbstractDecoratedEventHandler.dateFormatter);
        LocalDate endDate = LocalDate.parse(splits2[2], AbstractDecoratedEventHandler.dateFormatter);
        String avis2 = splits2[0];
        String roundTrip = splits2[3].replaceFirst("^RT", "");
    
    
    
        this.avis        = avis;
        this.roundTrip   = roundTrip;
        this.startDate   = startDate;
        this.endDate     = endDate;
        this.editionDate = editionDate;
        this.udgave      = udgave;
        this.sectionName = sectionName;
        this.pageNumber  = pageNumber;
    }
    
    public String getAvis() {
        return avis;
    }
    
    public String getRoundTrip() {
        return roundTrip;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public LocalDate getEditionDate() {
        return editionDate;
    }
    
    public String getUdgave() {
        return udgave;
    }
    
    public String getSectionName() {
        return sectionName;
    }
    
    public Integer getPageNumber() {
        return pageNumber;
    }
}
