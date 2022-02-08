package dk.kb.kula190.checkers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.kula190.iterators.eventhandlers.decorating.EditionBegins;
import dk.kb.kula190.iterators.eventhandlers.decorating.EditionEnds;
import dk.kb.kula190.iterators.eventhandlers.decorating.PageBegins;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NoMissingMiddlePagesChecker extends DecoratedEventHandler {
    private final ResultCollector resultCollector;
    
    public NoMissingMiddlePagesChecker(ResultCollector resultCollector) {
                                                                            super(resultCollector);
                                                                            this.resultCollector = resultCollector;}
    
    private List<Integer> pages;
    
    @Override
    public void editionBegins(EditionBegins event, String editionName) {
        pages = new ArrayList<>();
    }
    
    @Override
    public void pageBegins(PageBegins event, String editionName, Integer pageNumber) {
        pages.add(pageNumber);
    }
    
    
    @Override
    public void editionEnds(EditionEnds event, String editionName) {
        List<Integer> sortedPages = pages.stream().sorted().toList();
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
