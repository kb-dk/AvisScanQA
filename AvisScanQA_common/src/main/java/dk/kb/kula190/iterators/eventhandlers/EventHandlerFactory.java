package dk.kb.kula190.iterators.eventhandlers;

import java.util.List;

/**
 * Concrete classes defines a specific set of checkers to run on on a batch.
 */
public interface EventHandlerFactory {
    List<TreeEventHandler> createEventHandlers();
}
