package dk.kb.kula190.iterators.eventhandlers;

import dk.kb.kula190.Batch;
import dk.kb.kula190.MultiThreadedRunnableComponent;
import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.RunnableComponent;
import dk.kb.kula190.checkers.ChecksumChecker;
import dk.kb.kula190.checkers.DatabaseRegister;
import dk.kb.kula190.checkers.crosscheckers.NoMissingMiddlePagesChecker;
import dk.kb.kula190.checkers.crosscheckers.PageStructureChecker;
import dk.kb.kula190.checkers.crosscheckers.XpathCrossChecker;
import dk.kb.kula190.checkers.singlecheckers.*;
import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.common.TreeIterator;
import dk.kb.kula190.iterators.filesystem.transparent.TransparintingFileSystemIterator;
import org.junit.jupiter.api.Test;
import org.postgresql.Driver;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class MultiThreadedEventRunnerTest {
    
    
    
    private final File
            specificBatch
            = new File(System.getenv("HOME") + "/Projects/AvisScanQA/data/orig/modersmaalet_19060701_19060709_RT1");
    
    @Test
    void run() throws Exception {
        
        Path batchPath = specificBatch.toPath().toAbsolutePath();
        Batch batch = new Batch(batchPath.getFileName().toString(), batchPath);
        
        
        RunnableComponent component = new MultiThreadedRunnableComponent() {
            @Override
            protected List<TreeEventHandler> getCheckers(ResultCollector resultCollector) {
                return List.of(
                        //Simple Checkers
                        new ChecksumChecker(resultCollector),
                        
                        //Per file- checkers
                        new XmlSchemaChecker(resultCollector),
                        new TiffAnalyzer(resultCollector),
                        new TiffChecker(resultCollector),
                        new XpathAltoChecker(resultCollector),
                        new XpathMixChecker(resultCollector),
                        new ModsChecker(resultCollector),
                        //CrossCheckers
                        new XpathCrossChecker(resultCollector),
                        new NoMissingMiddlePagesChecker(resultCollector),
                        new PageStructureChecker(resultCollector)
                
                
                              );
            }
            
        };
        
        ResultCollector resultCollector = component.doWorkOnItem(batch);
        
        System.out.println(resultCollector.toReport());
        
        
        
        
    }
}
