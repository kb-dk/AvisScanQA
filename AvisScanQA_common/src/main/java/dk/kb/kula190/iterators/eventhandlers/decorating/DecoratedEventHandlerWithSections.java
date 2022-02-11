package dk.kb.kula190.iterators.eventhandlers.decorating;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeParsingEvent;

import java.io.IOException;
import java.time.LocalDate;

public class DecoratedEventHandlerWithSections extends AbstractDecoratedEventHandlerWithSections {
    
    
    public DecoratedEventHandlerWithSections(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    @Override
    public void sectionBegins(NodeParsingEvent event, String avis, LocalDate editionDate, String udgave, String section)
            throws IOException {}
    
    @Override
    public void sectionEnds(NodeParsingEvent event, String avis, LocalDate editionDate, String udgave, String section)
            throws IOException {}
    
    @Override
    public void pageBegins(NodeParsingEvent event,
                           String avis,
                           LocalDate editionDate,
                           String udgave,
                           String sectionName,
                           Integer pageNumber) throws IOException {
        
    }
    
    @Override
    public void pageEnds(NodeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        
    }
    
    @Override
    public void mixFile(AttributeParsingEvent event,
                        String avis,
                        LocalDate editionDate,
                        String udgave,
                        String sectionName,
                        Integer pageNumber) throws IOException {
        
    }
    
    @Override
    public void tiffFile(AttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        
    }
    
    @Override
    public void altoFile(AttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {}
    
    @Override
    public void pdfFile(AttributeParsingEvent event,
                        String avis,
                        LocalDate editionDate,
                        String udgave,
                        String sectionName,
                        Integer pageNumber) throws IOException {}
    
    
    public void batchBegins(NodeParsingEvent event,
                            String avis,
                            String roundTrip,
                            LocalDate startDate,
                            LocalDate endDate) throws IOException {}
    
    
    public void batchEnds(NodeParsingEvent event,
                          String avis,
                          String roundTrip,
                          LocalDate startDate,
                          LocalDate endDate) throws IOException {}
    
    
    public void modsBegins(NodeParsingEvent event,
                           String avis,
                           String roundTrip,
                           LocalDate startDate,
                           LocalDate endDate) throws IOException {}
    
    public void modsFile(AttributeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {}
    
    public void modsEnds(NodeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {}
    
    
    public void metsBegins(NodeParsingEvent event,
                           String avis,
                           String roundTrip,
                           LocalDate startDate,
                           LocalDate endDate) throws IOException {}
    
    
    public void metsFile(AttributeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {}
    
    public void metsEnds(NodeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {}
    
    
    public void editionBegins(NodeParsingEvent event,
                              String avis,
                              LocalDate editionDate,
                              String editionName) throws IOException {}
    
    public void editionEnds(NodeParsingEvent event,
                            String avis,
                            LocalDate editionDate,
                            String editionName) throws IOException {}
    
    
    public final void pageBegins(NodeParsingEvent event,
                                 String avis,
                                 LocalDate editionDate,
                                 String udgave, Integer pageNumber) throws IOException {}
    
    public final void pageEnds(NodeParsingEvent event,
                               String avis,
                               LocalDate editionDate,
                               String udgave, Integer pageNumber) throws IOException {}
    
    
    public final void mixFile(AttributeParsingEvent event,
                              String avis,
                              LocalDate editionDate,
                              String udgave, Integer pageNumber) throws IOException {}
    
    public final void tiffFile(AttributeParsingEvent event,
                               String avis,
                               LocalDate editionDate,
                               String udgave, Integer pageNumber) throws IOException {}
    
    public final void altoFile(AttributeParsingEvent event,
                               String avis,
                               LocalDate editionDate,
                               String udgave, Integer pageNumber) throws IOException {}
    
    public final void pdfFile(AttributeParsingEvent event,
                              String avis,
                              LocalDate editionDate,
                              String udgave, Integer pageNumber) throws IOException {}
    
    
}
