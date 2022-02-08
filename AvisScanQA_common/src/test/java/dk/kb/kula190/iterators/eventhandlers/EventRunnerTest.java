package dk.kb.kula190.iterators.eventhandlers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.TreeIterator;
import dk.kb.kula190.iterators.filesystem.transparent.TransparintingFileSystemIterator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

class EventRunnerTest {
    
    private TreeIterator iterator;
    
    
    public TreeIterator getIterator() throws URISyntaxException {
        if (iterator == null) {
            //File file = new File(Thread.currentThread().getContextClassLoader().getResource("batch").toURI());
            File file = new File("/home/abr/Projects/AvisScanQA/data/modersmaalet_19060701_19061231_RT1");
            
            System.out.println(file);
            iterator = new TransparintingFileSystemIterator(file,
                                                            file.getParentFile(),
                                                            List.of("MIX", "TIFF", "PDF", "ALTO"),
                                                            "_[^_]+$",
                                                            "\\.[^_]+$");
        }
        return iterator;
        
    }
    
    @Test
    void run() throws URISyntaxException {
        ResultCollector resultCollector = new ResultCollector("Testing tool","Testing version", 100);
        List<TreeEventHandler> eventHandlers = List.of(new ChecksumEventHandler(resultCollector));
    
        EventRunner runner = new EventRunner(getIterator(), eventHandlers, resultCollector);
        runner.run();
    
        System.out.println(resultCollector.toReport());
        //new
    }
}
