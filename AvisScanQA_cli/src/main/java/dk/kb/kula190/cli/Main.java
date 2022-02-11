package dk.kb.kula190.cli;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.checkers.ChecksumChecker;
import dk.kb.kula190.checkers.MixXmlSchemaChecker;
import dk.kb.kula190.checkers.NoMissingMiddlePagesChecker;
import dk.kb.kula190.checkers.PageStructureChecker;
import dk.kb.kula190.iterators.common.TreeIterator;
import dk.kb.kula190.iterators.eventhandlers.EventRunner;
import dk.kb.kula190.iterators.eventhandlers.TreeEventHandler;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedConsoleLogger;
import dk.kb.kula190.iterators.filesystem.transparent.TransparintingFileSystemIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
        ResultCollector resultCollector = new ResultCollector("Testing tool", "Testing version", 100);
        
        try (PrintStream out = new PrintStream("decoratedBatch.xml")) {
            
            List<TreeEventHandler> eventHandlers = List.of(
                    new ChecksumChecker(resultCollector),
                    new NoMissingMiddlePagesChecker(resultCollector),
                    new PageStructureChecker(resultCollector),
                    new MixXmlSchemaChecker(resultCollector),
                    new DecoratedConsoleLogger(out, resultCollector));
            
            TreeIterator iterator = getIterator(args[0]);
            EventRunner runner = new EventRunner(iterator, eventHandlers, resultCollector);
            
            runner.run();
        }
        System.out.println(resultCollector.toReport());
    }
    
    public static TreeIterator getIterator(String pathname) throws URISyntaxException {
        
        //File specificBatch = new File(Thread.currentThread().getContextClassLoader().getResource("batch").toURI());
        File specificBatch = new File(pathname);
        
        System.out.println(specificBatch);
        
        File batchesFolder = specificBatch.getParentFile();
        
        TreeIterator iterator = new TransparintingFileSystemIterator(
                //Folder for the specific batch to run on
                specificBatch,
                
                //Folder where the batches reside. Nessesary to see what part of the specific batch is name
                batchesFolder,
                
                //These folders will NOT be nodes, but regarded as transparent
                List.of("MIX", "TIFF", "PDF", "ALTO"),
                
                //actual files named as modersmaalet_19060703_udg01_1.sektion_0004.mix.xml
                List.of(
                        //Part to remove to generate the edition name
                        //will output modersmaalet_19060703_udg01
                        "_[^_]+_\\d{4}\\.\\w+(\\.xml)?$"
                        
                        //Part to remove to generate the section name
                        //will output modersmaalet_19060703_udg01_1.sektion
                        , "_[^_]+$", //filename.split(editionRegexp)[0];
                        
                        //Part to remove to generate the page name
                        //will output modersmaalet_19060703_udg01_1.sektion_0004
                        "\\.[^_]+$" //filename.split(pageRegexp)[0];
                       ),
                
                
                //How to adapt the filename for the checksum extension below
                "\\.[^_]+$",
                
                ".md5");
        
        return iterator;
        
    }
}