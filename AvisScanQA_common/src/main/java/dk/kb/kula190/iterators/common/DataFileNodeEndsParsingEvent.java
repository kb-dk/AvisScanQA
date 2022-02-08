package dk.kb.kula190.iterators.common;

public class DataFileNodeEndsParsingEvent extends NodeEndParsingEvent {

    public DataFileNodeEndsParsingEvent(String name) {
            super(name, null);
        }

    public DataFileNodeEndsParsingEvent(String name, String location) {
        super(name, location);
    }
}
