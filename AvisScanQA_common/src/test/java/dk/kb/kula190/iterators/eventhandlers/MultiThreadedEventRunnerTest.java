package dk.kb.kula190.iterators.eventhandlers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.checkers.ChecksumChecker;
import dk.kb.kula190.checkers.crosscheckers.NoMissingMiddlePagesChecker;
import dk.kb.kula190.checkers.crosscheckers.PageStructureChecker;
import dk.kb.kula190.checkers.crosscheckers.XpathCrossChecker;
import dk.kb.kula190.checkers.singlecheckers.TiffChecker;
import dk.kb.kula190.checkers.singlecheckers.XmlSchemaChecker;
import dk.kb.kula190.checkers.singlecheckers.XpathAltoChecker;
import dk.kb.kula190.checkers.singlecheckers.XpathMixChecker;
import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.common.TreeIterator;
import dk.kb.kula190.iterators.filesystem.transparent.TransparintingFileSystemIterator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class MultiThreadedEventRunnerTest {
    
    private TreeIterator iterator;
    
    
    public TreeIterator getIterator() throws URISyntaxException {
        if (iterator == null) {
            //File file = new File(Thread.currentThread().getContextClassLoader().getResource("batch").toURI());
            File file = new File(System.getenv("HOME")+"/Projects/AvisScanQA/data/orig/modersmaalet_19060701_19061231_RT1");
            
            System.out.println(file);
            iterator = new TransparintingFileSystemIterator(file,
                                                            file.getParentFile(),
                                                            List.of("MIX", "TIFF", "PDF", "ALTO"),
                                                            List.of("_[^_]+$",
                                                            "\\.[^_]+$"),
                                                            "\\.[^_]+$",
                                                            ".md5");
        }
        return iterator;
        
    }
    
    @Test
    void run() throws URISyntaxException {
        ResultCollector resultCollector = new ResultCollector("Testing tool", "Testing version", 100);
        
        
        List<TreeEventHandler> eventHandlers = List.of(
                //Simple Checkers
                new ChecksumChecker(resultCollector),
        
                //Per file- checkers
                new XmlSchemaChecker(resultCollector),
                //new TiffChecker(resultCollector),
                new XpathAltoChecker(resultCollector),
                new XpathMixChecker(resultCollector),
        
                //CrossCheckers
                new XpathCrossChecker(resultCollector),
                new NoMissingMiddlePagesChecker(resultCollector),
                new PageStructureChecker(resultCollector)
                                                       );

        
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
        
        //Use 4 concurrent threads
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        
        EventRunner runner = new MultiThreadedEventRunner(getIterator(),
                                                          eventHandlers,
                                                          resultCollector,
                                                          forkOnEdition,
                                                          executorService);
        
        runner.run();
        
        System.out.println(resultCollector.toReport());
        //new
    }
}
