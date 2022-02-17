package dk.kb.kula190.checkers.crosscheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedNodeParsingEvent;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PageStructureChecker extends DecoratedEventHandler {
    private ThreadLocal<Set<String>> types = new ThreadLocal<>();
    
    public PageStructureChecker(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    
    @Override
    public void pageBegins(DecoratedNodeParsingEvent event,
                           String avis,
                           LocalDate editionDate,
                           String udgave,
                           String sectionName,
                           Integer pageNumber) throws IOException {
        //Checkes that each page consists of MIX ALTO TIFF
        types.set(new HashSet<>());
    }
    
    @Override
    public void pageEnds(DecoratedNodeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        final Set<String> strings = types.get();
        if (!strings.containsAll(Set.of("MIX", "ALTO", "TIFF"))) {
            addFailure(event,
                                            "Missing files per page",
                                            this.getClass().getSimpleName(),
                                            "Must contain mix, alto and tiff files",
                                            strings.stream().map(x -> "" + x).collect(Collectors.joining(",")));
        }
    }
    
    
    @Override
    public void mixFile(DecoratedAttributeParsingEvent event,
                        String avis,
                        LocalDate editionDate,
                        String udgave,
                        String sectionName,
                        Integer pageNumber) throws IOException {
        types.get().add("MIX");
    }
    
    @Override
    public void altoFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        types.get().add("ALTO");
    }
    
    @Override
    public void tiffFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        types.get().add("TIFF");
    }
}
