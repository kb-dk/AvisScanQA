package dk.kb.kula190.iterators.eventhandlers.decorating;

import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;

public class EditionBegins extends NodeBeginsParsingEvent {
    public EditionBegins(String name) {
        super(name);
    }
    
    public EditionBegins(String name, String location) {
        super(name, location);
    }
}
