package dk.kb.kula190.iterators.eventhandlers.decorating;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.ParsingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;

public class DecoratedEventHandler extends AbstractDecoratedEventHandler {
    
    public DecoratedEventHandler(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    @Override
    public void batchBegins(DecoratedNodeParsingEvent event,
                            String avis,
                            String roundTrip,
                            LocalDate startDate,
                            LocalDate endDate) throws IOException {}
    
    @Override
    public void batchEnds(DecoratedNodeParsingEvent event,
                          String avis,
                          String roundTrip,
                          LocalDate startDate,
                          LocalDate endDate) throws IOException {}
    
    @Override
    public void modsBegins(DecoratedNodeParsingEvent event,
                           String avis,
                           String roundTrip,
                           LocalDate startDate,
                           LocalDate endDate) throws IOException {}
    @Override
    public void modsFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {}
    @Override
    public void modsEnds(DecoratedNodeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {}
    
    @Override
    public void metsBegins(DecoratedNodeParsingEvent event,
                           String avis,
                           String roundTrip,
                           LocalDate startDate,
                           LocalDate endDate) throws IOException {}
    
    @Override
    public void metsFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {}
    @Override
    public void metsEnds(DecoratedNodeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {}
    
    @Override
    public void editionBegins(DecoratedNodeParsingEvent event,
                              String avis,
                              LocalDate editionDate,
                              String editionName) throws IOException {}
    @Override
    public void editionEnds(DecoratedNodeParsingEvent event,
                            String avis,
                            LocalDate editionDate,
                            String editionName) throws IOException {}
    
    
    
    @Override
    public void sectionBegins(DecoratedNodeParsingEvent event,
                              String avis,
                              LocalDate editionDate,
                              String udgave,
                              String section) throws IOException {
        
    }
    
    @Override
    public void sectionEnds(DecoratedNodeParsingEvent event,
                            String avis,
                            LocalDate editionDate,
                            String udgave,
                            String section) throws IOException {
        
    }
    
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
    public void mixFile(DecoratedAttributeParsingEvent event,
                        String avis,
                        LocalDate editionDate,
                        String udgave,
                        String sectionName,
                        Integer pageNumber) throws IOException {
        
    }
    
    @Override
    public void tiffFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        
    }
    
    @Override
    public void altoFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        
    }
    
    @Override
    public void pdfFile(DecoratedAttributeParsingEvent event,
                        String avis,
                        LocalDate editionDate,
                        String udgave,
                        String sectionName,
                        Integer pageNumber) throws IOException {
        
    }
    
    @Override
    public void injectedFile(DecoratedAttributeParsingEvent decoratedEvent,
                             String injectedType, String avis,
                             LocalDate editionDate,
                             String udgave,
                             String sectionName,
                             Integer pageNumber) throws IOException {
        
    }
}
