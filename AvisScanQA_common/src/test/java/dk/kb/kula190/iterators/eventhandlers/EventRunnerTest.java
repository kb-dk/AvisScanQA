package dk.kb.kula190.iterators.eventhandlers;

import dk.kb.kula190.Batch;
import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.RunnableComponent;
import dk.kb.kula190.checkers.ChecksumChecker;
import dk.kb.kula190.checkers.DatabaseRegister;
import dk.kb.kula190.checkers.crosscheckers.NoMissingMiddlePagesChecker;
import dk.kb.kula190.checkers.crosscheckers.PageStructureChecker;
import dk.kb.kula190.checkers.crosscheckers.XpathCrossChecker;
import dk.kb.kula190.checkers.singlecheckers.TiffAnalyzer;
import dk.kb.kula190.checkers.singlecheckers.TiffChecker;
import dk.kb.kula190.checkers.singlecheckers.XmlSchemaChecker;
import dk.kb.kula190.checkers.singlecheckers.XpathAltoChecker;
import dk.kb.kula190.checkers.singlecheckers.XpathMixChecker;
import dk.kb.kula190.iterators.common.TreeIterator;
import org.junit.jupiter.api.Test;
import org.postgresql.Driver;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

class EventRunnerTest {
    
    private final File
            specificBatch
            = new File(System.getenv("HOME") + "/Projects/AvisScanQA/data/orig/modersmaalet_19060701_19061231_RT1");
  
    
    @Test
    void run() throws Exception {
    
        Path batchPath = specificBatch.toPath().toAbsolutePath();
        Batch batch = new Batch(batchPath.getFileName().toString(), batchPath);
    
    
        RunnableComponent component = new RunnableComponent() {
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
