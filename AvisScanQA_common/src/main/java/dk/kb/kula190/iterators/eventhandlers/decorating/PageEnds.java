package dk.kb.kula190.iterators.eventhandlers.decorating;

import dk.kb.kula190.iterators.common.NodeEndParsingEvent;

public class PageEnds extends NodeEndParsingEvent {
    public PageEnds(String name) {
        super(name);
    }
    
    public PageEnds(String name, String location) {
        super(name, location);
    }
}
