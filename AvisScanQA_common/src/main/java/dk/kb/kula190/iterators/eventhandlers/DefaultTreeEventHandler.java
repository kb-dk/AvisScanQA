package dk.kb.kula190.iterators.eventhandlers;


import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;
import dk.kb.kula190.iterators.common.NodeEndParsingEvent;

/** Abstract tree event handler, with no-op methods */
public abstract class DefaultTreeEventHandler implements TreeEventHandler {



    public void handleFinish() {
    }

    public void handleNodeBegin(NodeBeginsParsingEvent event) {
    }

    public void handleNodeEnd(NodeEndParsingEvent event) {
    }

    public void handleAttribute(AttributeParsingEvent event) {
    }
}
