package dk.kb.kula190.iterators.eventhandlers;

import dk.kb.kula190.Batch;
import dk.kb.kula190.DecoratedRunnableComponent;
import dk.kb.kula190.MultiThreadedRunnableComponent;
import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.checkers.batchcheckers.MetsChecker;
import dk.kb.kula190.checkers.batchcheckers.MetsSplitter;
import dk.kb.kula190.checkers.editioncheckers.NoMissingMiddlePagesChecker;
import dk.kb.kula190.checkers.filecheckers.XmlSchemaChecker;
import dk.kb.kula190.checkers.filecheckers.tiff.TiffAnalyzerExiv2;
import dk.kb.kula190.checkers.filecheckers.tiff.TiffAnalyzerImageMagick;
import dk.kb.kula190.checkers.filecheckers.tiff.TiffCheckerExiv2;
import dk.kb.kula190.checkers.filecheckers.tiff.TiffCheckerImageMagick;
import dk.kb.kula190.checkers.pagecheckers.PageStructureChecker;
import dk.kb.kula190.checkers.pagecheckers.XpathPageChecker;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;

class MultiThreadedEventRunnerTest {
    
    
    private final File
            specificBatch
            = new File(System.getenv("HOME") + "/Projects/AvisScanQA/data/orig/modersmaalet_19060701_19061231_RT1");
    
    @Test
    void run() throws Exception {
        
        Path batchPath = specificBatch.toPath().toAbsolutePath();
        Batch batch = new Batch(batchPath.getFileName().toString(), batchPath);
        
        
        DecoratedRunnableComponent component = new MultiThreadedRunnableComponent(Executors.newFixedThreadPool(4)) {
            @Override
            protected List<TreeEventHandler> getCheckers(ResultCollector resultCollector) {
                return List.of(
                        
                         new TiffAnalyzerExiv2(resultCollector),
                         new TiffCheckerExiv2(resultCollector),
                        //
                         new TiffAnalyzerImageMagick(resultCollector),
                         new TiffCheckerImageMagick(resultCollector)
                        //
//                        new MetsSplitter(resultCollector),
//                        new MetsChecker(resultCollector),
                        
                        //Per file- checkers
//                        new XmlSchemaChecker(resultCollector)
                        
                        
                        //CrossCheckers
                        // new XpathPageChecker(resultCollector),
                        // new NoMissingMiddlePagesChecker(resultCollector),
                        // new PageStructureChecker(resultCollector)
                
                
                              );
            }
            
        };
        
        ResultCollector resultCollector = component.doWorkOnItem(batch);
        
        System.out.println(resultCollector.toReport());
        
        
    }
}
