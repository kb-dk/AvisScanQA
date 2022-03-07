package dk.kb.kula190.iterators.eventhandlers;

import dk.kb.kula190.BasicRunnableComponent;
import dk.kb.kula190.Batch;
import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.RunnableComponent;
import dk.kb.kula190.checkers.simplecheckers.FileNamingChecker;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

class BasicRunnableComponentTest {
    
    
    
    private final File
            specificBatch
            = new File(System.getenv("HOME") + "/Projects/AvisScanQA/data/orig/modersmaalet_19060701_19060709_RT1");
    
    @Test
    void run() throws Exception {
        
        Path batchPath = specificBatch.toPath().toAbsolutePath();
        Batch batch = new Batch(batchPath.getFileName().toString(), batchPath);
    
        RunnableComponent component =
                new BasicRunnableComponent((resultCollector, treeEventHandlers, treeIterator) -> new EventRunner(
                        treeIterator,
                        treeEventHandlers,
                        resultCollector)) {
                    //TODO Why both override and functional interface? Cleanup this mess
                    @Override
                    protected List<TreeEventHandler> getCheckers(ResultCollector resultCollector) {
                        return List.of(
                                //Simple Checkers
                                // new ChecksumChecker(resultCollector),
                                new FileNamingChecker(resultCollector)
                                      );
                    }
                };
    
    
        ResultCollector resultCollector = component.doWorkOnItem(batch);
    
        System.out.println(resultCollector.toReport());
    
    }
}
