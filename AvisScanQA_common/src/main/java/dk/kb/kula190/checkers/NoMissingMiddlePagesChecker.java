package dk.kb.kula190.checkers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;
import dk.kb.kula190.iterators.common.NodeEndParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.DefaultTreeEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NoMissingMiddlePagesChecker extends DefaultTreeEventHandler {
    private final ResultCollector resultCollector;
    
    public NoMissingMiddlePagesChecker(ResultCollector resultCollector) {this.resultCollector = resultCollector;}
    
    private int level = 0;
    
    private List<Integer> pages;
    
    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        if (isEdition(event)) {
            //startCounter;
            
            pages = new ArrayList<>();
            
        }
        if (isPage(event)) {
            //incrementCounter
            pages.add(Integer.parseInt(event.getName().replaceFirst("^(.+?)_(\\d+)$", "$2")));
        }
        level += 1;
    }
    
    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        level -= 1;
    
        if (isEdition(event)) {
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
    
    private boolean isEdition(ParsingEvent event) {
        return level == 1 && !Set.of("METS","MODS").contains(lastName(event.getName()));
    }
    
    private String lastName(String name) {
        return name.replaceFirst("^(.+?)/([^/]+)$","$2");
    }
    
    private boolean isPage(ParsingEvent event) {
        return level == 2;
    }
    
}
