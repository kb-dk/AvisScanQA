package dk.kb.kula190.checkers.crosscheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedNodeParsingEvent;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PageStructureChecker extends DecoratedEventHandler {

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
        Map<String, Object> env = registerEnv(avis,
                                              editionDate.toString(),
                                              udgave,
                                              sectionName,
                                              pageNumber.toString());
        
        env.put("types", new HashSet<String>());
        //Checkes that each page consists of MIX ALTO TIFF
    }

    @Override
    public void pageEnds(DecoratedNodeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        Map<String, Object> env = dropEnv(avis, editionDate.toString(), udgave, sectionName, pageNumber.toString());
        final Set<String> strings = (Set<String>) env.get("types");
        
        if (!strings.containsAll(Set.of("MIX", "ALTO", "TIFF"))) {
            addFailure(event,
                       FailureType.MISSING_FILE_ERROR,
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
        Map<String, Object> env = retriveEnv(avis, editionDate.toString(), udgave, sectionName, pageNumber.toString());
        final Set<String> strings = (Set<String>) env.get("types");
        strings.add("MIX");
    }

    @Override
    public void altoFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        Map<String, Object> env = retriveEnv(avis, editionDate.toString(), udgave, sectionName, pageNumber.toString());
        final Set<String> strings = (Set<String>) env.get("types");
        strings.add("ALTO");
    }

    @Override
    public void tiffFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        Map<String, Object> env = retriveEnv(avis, editionDate.toString(), udgave, sectionName, pageNumber.toString());
        final Set<String> strings = (Set<String>) env.get("types");
        strings.add("TIFF");
    }
}
