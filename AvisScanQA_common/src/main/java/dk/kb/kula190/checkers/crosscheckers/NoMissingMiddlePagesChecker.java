package dk.kb.kula190.checkers.crosscheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedNodeParsingEvent;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NoMissingMiddlePagesChecker extends DecoratedEventHandler {
    private final ResultCollector resultCollector;
    
    public NoMissingMiddlePagesChecker(ResultCollector resultCollector) {
        super(resultCollector);
        this.resultCollector = resultCollector;
    }
    
    @Override
    public void sectionBegins(DecoratedNodeParsingEvent event, String avis, LocalDate editionDate, String udgave, String sectionName)
            throws IOException {
        Map<String, Object> env = registerEnv(avis, editionDate.toString(), udgave, sectionName);
        env.put("pages", new ArrayList<>());
    }
    
    @Override
    public void pageBegins(DecoratedNodeParsingEvent event,
                           String avis,
                           LocalDate editionDate,
                           String udgave,
                           String sectionName,
                           Integer pageNumber) {
        Map<String, Object> env = retriveEnv(avis, editionDate.toString(), udgave, sectionName);
        List<Integer> integers = (List<Integer>) env.get("pages");
        integers.add(pageNumber);
    }
    
    @Override
    public void sectionEnds(DecoratedNodeParsingEvent event, String avis, LocalDate editionDate, String udgave, String sectionName)
            throws IOException {
        Map<String, Object> env = dropEnv(avis, editionDate.toString(), udgave, sectionName);
        List<Integer> integers = (List<Integer>) env.get("pages");
        List<Integer> sortedPages = integers.stream().sorted().toList();
        for (int i = 0; i < sortedPages.size() - 1; i++) {
            if (sortedPages.get(i) + 1 != sortedPages.get(i + 1)) {
                resultCollector.addFailure(event,
                                           FailureType.MISSING_FILE_ERROR,
                                           this.getClass().getSimpleName(),
                                           "Section have gaps in page sequence",
                                           sortedPages.stream().map(x -> "" + x).collect(Collectors.joining(",")));
            }
        }
    }
}
