package dk.kb.kula190.iterators.eventhandlers;

import dk.kb.kula190.Batch;
import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.RunnableComponent;
import dk.kb.kula190.checkers.crosscheckers.MetsChecker;
import dk.kb.kula190.checkers.crosscheckers.PageStructureChecker;
import dk.kb.kula190.checkers.singlecheckers.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

class EventRunnerTest {
    
    private final File
            specificBatch
            = new File(System.getenv("HOME") + "/Projects/AvisScanQA/data/orig/modersmaalet_19060701_19060709_RT1");
  
    
    @Test
    void run() throws Exception {
    
        Path batchPath = specificBatch.toPath().toAbsolutePath();
        Batch batch = new Batch(batchPath.getFileName().toString(), batchPath);
    
    
        RunnableComponent component = new RunnableComponent() {
            @Override
            protected List<TreeEventHandler> getCheckers(ResultCollector resultCollector) {
                return List.of(
                        //                        new TiffAnalyzer(resultCollector),
                        new MetsSplitter(resultCollector),
                        new MetsChecker(resultCollector),
        
        
                        //Simple Checkers
//                        new ChecksumChecker(resultCollector),
                        
                        //Per file- checkers
                     //   new XmlSchemaChecker(resultCollector),
                       // new TiffChecker(resultCollector),
                      //  new XpathAltoChecker(resultCollector),
                       // new XpathMixChecker(resultCollector),
//                            new ModsChecker(resultCollector),
                        //CrossCheckers
//                        new XpathCrossChecker(resultCollector)
                       // new NoMissingMiddlePagesChecker(resultCollector),
                        new PageStructureChecker(resultCollector)

                              );
            }
            
        };
    
        ResultCollector resultCollector = component.doWorkOnItem(batch);
        
        System.out.println(resultCollector.toReport());
        
        
      
    }
}
