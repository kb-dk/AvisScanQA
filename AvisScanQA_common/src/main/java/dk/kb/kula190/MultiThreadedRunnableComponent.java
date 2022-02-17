package dk.kb.kula190;

import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.EventRunner;
import dk.kb.kula190.iterators.eventhandlers.MultiThreadedEventRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class MultiThreadedRunnableComponent extends RunnableComponent{
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public ResultCollector doWorkOnItem(Batch batch) {
    
        
    
        ResultCollector resultCollector = new ResultCollector(getComponentName(), getComponentVersion(), null);
    
        log.info("Starting validation of '{}'", batch.getFullID());
        
    
        EventRunner runner = new MultiThreadedEventRunner(getIterator(batch.getLocation()),
                                                          getCheckers(resultCollector),
                                                          resultCollector,
                                                          getForkCondition(),
                                                          getExecutorService());
    
        runner.run();
        return resultCollector;
    }
    
    protected MultiThreadedEventRunner.EventCondition getForkCondition() {
        MultiThreadedEventRunner.EventCondition forkOnEdition = new MultiThreadedEventRunner.EventCondition() {
            private int level = 0;
        
            @Override
            public boolean shouldFork(ParsingEvent event) {
                level = event.getName().split("/").length;
                return level == 2; //level 2 is editions
            }
        
            @Override
            public boolean shouldJoin(ParsingEvent event) {
                level = event.getName().split("/").length;
                return level == 1; //level 1 is batch
            }
            //What this means is that we each edition is handled in a new thread,
            // and when we get to the end of the batch, we join back the threads
        };
        return forkOnEdition;
    }
    
    protected ExecutorService getExecutorService() {
        //Use 4 concurrent threads
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        return executorService;
    }
}
