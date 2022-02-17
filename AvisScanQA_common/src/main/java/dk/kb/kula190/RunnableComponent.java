package dk.kb.kula190;

import dk.kb.kula190.iterators.common.TreeIterator;
import dk.kb.kula190.iterators.eventhandlers.EventRunner;
import dk.kb.kula190.iterators.eventhandlers.TreeEventHandler;
import dk.kb.kula190.iterators.filesystem.transparent.TransparintingFileSystemIterator;
import org.slf4j.Logger;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

public abstract class RunnableComponent {
    private static Logger log = org.slf4j.LoggerFactory.getLogger(RunnableComponent.class);
    
    
    public final String getComponentName() {
        return getClass().getSimpleName();
    }
    
    public final String getComponentVersion() {
        return getClass().getPackage().getImplementationVersion();
    }
    
    public ResultCollector doWorkOnItem(Batch batch)
            throws
            Exception {
        
        ResultCollector resultCollector = new ResultCollector(getComponentName(), getComponentVersion(), 100);
        
        log.info("Starting validation of '{}'", batch.getFullID());
        
        List<TreeEventHandler> eventHandlers = getCheckers(resultCollector);
        
        TreeIterator iterator = getIterator(batch.getLocation());
        EventRunner runner = new EventRunner(iterator, eventHandlers, resultCollector);
        
        runner.run();
        return resultCollector;
        
    }
    
    
    protected TreeIterator getIterator(Path pathname) {
        
        File specificBatch = pathname.toFile();
        
        System.out.println(specificBatch);
        
        File batchesFolder = specificBatch.getParentFile();
        
        List<String> expressionsToMapFilenamesToStructure = List.of(
                //Part to remove to generate the edition name
                //will output modersmaalet_19060703_udg01
                "_[^_]+_\\d{4}\\.\\w+(\\.xml)?$"
                
                //Part to remove to generate the section name
                //will output modersmaalet_19060703_udg01_1.sektion
                , "_[^_]+$" //filename.split(editionRegexp)[0];
                
                //Part to remove to generate the page name
                //will output modersmaalet_19060703_udg01_1.sektion_0004
                , "\\.[^_]+$" //filename.split(pageRegexp)[0];
                                                                               );
    
        TreeIterator iterator = new TransparintingFileSystemIterator(
                //Folder for the specific batch to run on
                specificBatch,
                
                //Folder where the batches reside. Nessesary to see what part of the specific batch is name
                batchesFolder,
                
                //These folders will NOT be nodes, but regarded as transparent
                List.of("MIX", "TIFF", "PDF", "ALTO"),
                
                //actual files named as modersmaalet_19060703_udg01_1.sektion_0004.mix.xml
                expressionsToMapFilenamesToStructure,
                
                
                //How to adapt the filename for the checksum extension below
                "\\.[^_]+$",
                
                ".md5");
        
        return iterator;
        
    }
    
    protected abstract List<TreeEventHandler> getCheckers(ResultCollector resultCollector);
    
}
