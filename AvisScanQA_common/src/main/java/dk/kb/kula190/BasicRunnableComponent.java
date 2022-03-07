package dk.kb.kula190;

import dk.kb.kula190.iterators.common.TreeIterator;
import dk.kb.kula190.iterators.eventhandlers.EventRunner;
import dk.kb.kula190.iterators.eventhandlers.MultiThreadedEventRunner;
import dk.kb.kula190.iterators.eventhandlers.TreeEventHandler;
import dk.kb.kula190.iterators.filesystem.SimpleIteratorForFilesystems;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public abstract class BasicRunnableComponent extends RunnableComponent{
    private static Logger log = org.slf4j.LoggerFactory.getLogger(BasicRunnableComponent.class);
    
    private final TriFunction<ResultCollector, List<TreeEventHandler>, TreeIterator, EventRunner> eventRunnerFactory;
    
    //TODO make this work with MultiThreadEventRunner...
    public BasicRunnableComponent(TriFunction<ResultCollector, List<TreeEventHandler>, TreeIterator, EventRunner> eventRunnerFactory) {
        this.eventRunnerFactory = eventRunnerFactory;
    }
    
    public ResultCollector doWorkOnItem(Batch batch)
            throws
            Exception {
    
        ResultCollector resultCollector = new ResultCollector(getComponentName(), getComponentVersion(), 100);
    
        log.info("Starting validation of '{}'", batch.getFullID());
    
        List<TreeEventHandler> eventHandlers = getCheckers(resultCollector);
    
        TreeIterator iterator = getIterator(batch.getLocation());
        EventRunner runner = eventRunnerFactory.apply(resultCollector,eventHandlers,iterator);
        runner.run();
        return resultCollector;
    }
    
   
    
    
    protected TreeIterator getIterator(Path pathname) {
        
        File specificBatch = pathname.toFile();
        
        System.out.println(specificBatch);
        
        File batchesFolder = specificBatch.getParentFile();
        
        
        TreeIterator iterator = new SimpleIteratorForFilesystems(specificBatch,
                                                                 //How to adapt the filename for the checksum extension below
                                                                 "\\.[^_]+$",
                                                                 ".md5");
        
        return iterator;
        
    }
    
    protected abstract List<TreeEventHandler> getCheckers(ResultCollector resultCollector);
    
}
