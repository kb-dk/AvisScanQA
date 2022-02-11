package dk.kb.kula190.checkers.sections;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.NodeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandlerWithSections;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NoMissingMiddlePagesChecker extends DecoratedEventHandlerWithSections {
    private final ResultCollector resultCollector;
    //Note that all fields in these checkers should be threadlocal. Otherwise they will not work on multithreaded runs
    //There is only one checker instance shared between all the threads
    private ThreadLocal<List<Integer>> pages = new ThreadLocal<>();
    
    public NoMissingMiddlePagesChecker(ResultCollector resultCollector) {
        super(resultCollector);
        this.resultCollector = resultCollector;
    }
    
    @Override
    public void sectionBegins(NodeParsingEvent event, String avis, LocalDate editionDate, String udgave, String section)
            throws IOException {
        pages.set(new ArrayList<>());
    }
    
    @Override
    public void pageBegins(NodeParsingEvent event,
                           String editionName,
                           LocalDate editionDate,
                           String udgave,
                           String sectionName,
                           Integer pageNumber) {
        pages.get().add(pageNumber);
    }
    
    @Override
    public void sectionEnds(NodeParsingEvent event, String avis, LocalDate editionDate, String udgave, String section)
            throws IOException {
        List<Integer> sortedPages = pages.get().stream().sorted().toList();
        for (int i = 0; i < sortedPages.size() - 1; i++) {
            if (sortedPages.get(i) + 1 != sortedPages.get(i + 1)) {
                resultCollector.addFailure(event.getName(),
                                           "MissingPages",
                                           this.getClass().getSimpleName(),
                                           "Section have gaps in page sequence",
                                           sortedPages.stream().map(x -> "" + x).collect(Collectors.joining(",")));
            }
        }
    }
}
