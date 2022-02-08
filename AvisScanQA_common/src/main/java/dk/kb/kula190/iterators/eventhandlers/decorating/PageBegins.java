package dk.kb.kula190.iterators.eventhandlers.decorating;

import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;

public class PageBegins extends NodeBeginsParsingEvent {
    public PageBegins(String name) {
        super(name);
    }
    
    public PageBegins(String name, String location) {
        super(name, location);
    }
}
