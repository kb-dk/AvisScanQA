package dk.kb.kula190.iterators.eventhandlers;

import dk.kb.kula190.Batch;
import dk.kb.kula190.DecoratedRunnableComponent;
import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.checkers.editioncheckers.NoMissingMiddlePagesChecker;
import dk.kb.kula190.checkers.pagecheckers.PageStructureChecker;
import dk.kb.kula190.checkers.pagecheckers.XpathPageChecker;
import dk.kb.kula190.checkers.batchcheckers.MetsSplitter;
import dk.kb.kula190.checkers.filecheckers.tiff.TiffAnalyzerImageMagick;
import dk.kb.kula190.checkers.filecheckers.XmlSchemaChecker;
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
        
        
        DecoratedRunnableComponent component = new DecoratedRunnableComponent() {
            @Override
            protected List<TreeEventHandler> getCheckers(ResultCollector resultCollector) {
                return List.of(
                        // new TiffAnalyzerExiv2(resultCollector),
                        // new TiffCheckerExiv2(resultCollector),
                        
                        new TiffAnalyzerImageMagick(resultCollector),
                        // new TiffCheckerImageMagick(resultCollector),
                        
                         new MetsSplitter(resultCollector),
                         // new MetsChecker(resultCollector),
                        
                        //Per file- checkers
                        new XmlSchemaChecker(resultCollector),
                        
                        // CrossCheckers
                        new XpathPageChecker(resultCollector),
                        new NoMissingMiddlePagesChecker(resultCollector),
                        new PageStructureChecker(resultCollector)
                
                              );
            }
            
        };
        
        ResultCollector resultCollector = component.doWorkOnItem(batch);
        
        System.out.println(resultCollector.toReport());
        
        
    }
}
