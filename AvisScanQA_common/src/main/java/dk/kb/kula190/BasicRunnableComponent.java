package dk.kb.kula190;

import dk.kb.kula190.iterators.common.TreeIterator;
import dk.kb.kula190.iterators.eventhandlers.EventRunner;
import dk.kb.kula190.iterators.eventhandlers.TreeEventHandler;
import dk.kb.kula190.iterators.filesystem.SimpleIteratorForFilesystems;
import org.apache.commons.lang3.function.TriFunction;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class BasicRunnableComponent {
    private static Logger log = org.slf4j.LoggerFactory.getLogger(BasicRunnableComponent.class);
    
    private final TriFunction<ResultCollector, List<TreeEventHandler>, TreeIterator, EventRunner> eventRunnerFactory;
    
    public BasicRunnableComponent() {
        this((resultCollector, treeEventHandlers, treeIterator) -> new EventRunner(
                treeIterator,
                treeEventHandlers,
                resultCollector));
    }
    
    public BasicRunnableComponent(TriFunction<ResultCollector, List<TreeEventHandler>, TreeIterator, EventRunner> eventRunnerFactory) {
        this.eventRunnerFactory = eventRunnerFactory;
    }
    
    
    public final String getComponentName() {
        return getClass().getSimpleName();
    }
    
    public final String getComponentVersion() {
        return getClass().getPackage().getImplementationVersion();
    }
    
    public ResultCollector
    doWorkOnItem(Batch batch)
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
    
   
    
    
    protected TreeIterator getIterator(Path pathname) throws IOException {
        
        File specificBatch = pathname.toFile();
        
        System.out.println(specificBatch);
        
        File batchesFolder = specificBatch.getParentFile();
        
        
        TreeIterator iterator = new SimpleIteratorForFilesystems(specificBatch,
                                                                 //How to adapt the filename for the checksum extension below
                                                                 "checksums.txt");
        
        return iterator;
        
    }
    
    protected abstract List<TreeEventHandler> getCheckers(ResultCollector resultCollector);
    
}
