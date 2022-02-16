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
    
    
    public DecoratedNodeParsingEvent(String name,
                                     ParsingEventType type,
                                     String location,
                                     String avis,
                                     String roundTrip,
                                     LocalDate startDate,
                                     LocalDate endDate,
                                     LocalDate editionDate,
                                     String udgave,
                                     String sectionName,
                                     Integer pageNumber) {
        super(name, type, location);
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
