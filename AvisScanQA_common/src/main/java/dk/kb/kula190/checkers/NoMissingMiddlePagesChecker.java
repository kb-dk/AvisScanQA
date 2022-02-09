package dk.kb.kula190.checkers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;
import dk.kb.kula190.iterators.common.NodeEndParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NoMissingMiddlePagesChecker extends DecoratedEventHandler {
    private final ResultCollector resultCollector;
    
    public NoMissingMiddlePagesChecker(ResultCollector resultCollector) {
        super(resultCollector);
        this.resultCollector = resultCollector;
    }
    
    //Note that all fields in these checkers should be threadlocal. Otherwise they will not work on multithreaded runs
    //There is only one checker instance shared between all the threads
    private ThreadLocal<List<Integer>> pages = new ThreadLocal<>();
    
    @Override
    public void editionBegins(ParsingEvent event, String avis, LocalDate editionDate, String editionName) {
        pages.set(new ArrayList<>());
    }
    
    @Override
    public void pageBegins(ParsingEvent event,
                           String editionName,
                           LocalDate editionDate,
                           String udgave,
                           String sectionName,
                           Integer pageNumber) {
        pages.get().add(pageNumber);
    }
    
    @Override
    public void editionEnds(ParsingEvent event, String avis, LocalDate editionDate, String editionName) {
        List<Integer> sortedPages = pages.get().stream().sorted().toList();
        for (int i = 0; i < sortedPages.size() - 1; i++) {
            if (sortedPages.get(i) + 1 != sortedPages.get(i + 1)) {
                resultCollector.addFailure(event.getName(),
                                           "MissingPages",
                                           this.getClass().getSimpleName(),
                                           "Edition have gaps in page sequence",
                                           sortedPages.stream().map(x -> "" + x).collect(Collectors.joining(",")));
            }
        }
    }
}
