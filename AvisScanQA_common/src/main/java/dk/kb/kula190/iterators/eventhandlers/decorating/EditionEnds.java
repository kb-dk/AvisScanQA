package dk.kb.kula190.iterators.eventhandlers.decorating;

import dk.kb.kula190.iterators.common.NodeEndParsingEvent;

public class EditionEnds extends NodeEndParsingEvent {
    public EditionEnds(String name) {
        super(name);
    }
    
    public EditionEnds(String name, String location) {
        super(name, location);
    }
}
