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
    public void sectionBegins(DecoratedNodeParsingEvent event, String avis, LocalDate editionDate, String udgave, String section)
            throws IOException {}
    
    @Override
    public void sectionEnds(DecoratedNodeParsingEvent event, String avis, LocalDate editionDate, String udgave, String section)
            throws IOException {}
    
    @Override
    public void pageBegins(DecoratedNodeParsingEvent event,
                           String avis,
                           LocalDate editionDate,
                           String udgave,
                           String sectionName,
                           Integer pageNumber) throws IOException {
        
    }
    
    @Override
    public void pageEnds(DecoratedNodeParsingEvent event,
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
    
    
    public void batchBegins(DecoratedNodeParsingEvent event,
                            String avis,
                            String roundTrip,
                            LocalDate startDate,
                            LocalDate endDate) throws IOException {}
    
    
    public void batchEnds(DecoratedNodeParsingEvent event,
                          String avis,
                          String roundTrip,
                          LocalDate startDate,
                          LocalDate endDate) throws IOException {}
    
    
    public void modsBegins(DecoratedNodeParsingEvent event,
                           String avis,
                           String roundTrip,
                           LocalDate startDate,
                           LocalDate endDate) throws IOException {}
    
    public void modsFile(AttributeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {}
    
    public void modsEnds(DecoratedNodeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {}
    
    
    public void metsBegins(DecoratedNodeParsingEvent event,
                           String avis,
                           String roundTrip,
                           LocalDate startDate,
                           LocalDate endDate) throws IOException {}
    
    
    public void metsFile(AttributeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {}
    
    public void metsEnds(DecoratedNodeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {}
    
    
    public void editionBegins(DecoratedNodeParsingEvent event,
                              String avis,
                              LocalDate editionDate,
                              String editionName) throws IOException {}
    
    public void editionEnds(DecoratedNodeParsingEvent event,
                            String avis,
                            LocalDate editionDate,
                            String editionName) throws IOException {}
    
    
    public final void pageBegins(DecoratedNodeParsingEvent event,
                                 String avis,
                                 LocalDate editionDate,
                                 String udgave, Integer pageNumber) throws IOException {}
    
    public final void pageEnds(DecoratedNodeParsingEvent event,
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
