package dk.kb.kula190.iterators.eventhandlers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.checkers.ChecksumChecker;
import dk.kb.kula190.checkers.MixXmlSchemaChecker;
import dk.kb.kula190.checkers.NoMissingMiddlePagesChecker;
import dk.kb.kula190.iterators.common.TreeIterator;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedConsoleLogger;
import dk.kb.kula190.iterators.filesystem.transparent.TransparintingFileSystemIterator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.List;

class EventRunnerTest {

    private TreeIterator iterator;


    public TreeIterator getIterator() throws URISyntaxException {
        if (iterator == null) {
            //File specificBatch = new File(Thread.currentThread().getContextClassLoader().getResource("batch").toURI());
            File specificBatch = new File("/home/pabr/Projects/AvisScanQA/data/modersmaalet_19060701_19061231_RT1");

            System.out.println(specificBatch);

            File batchesFolder = specificBatch.getParentFile();

            iterator = new TransparintingFileSystemIterator(
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
        }
        return iterator;

    }

    @Test
    void run() throws URISyntaxException, FileNotFoundException {
        ResultCollector resultCollector = new ResultCollector("Testing tool", "Testing version", 100);

        try (PrintStream out = new PrintStream("decoratedBatch.xml")) {

            List<TreeEventHandler> eventHandlers = List.of(//new ChecksumChecker(resultCollector),
                    //new NoMissingMiddlePagesChecker(resultCollector),
                    new MixXmlSchemaChecker(resultCollector),
                    new DecoratedConsoleLogger(out, resultCollector));

            EventRunner runner = new EventRunner(getIterator(), eventHandlers, resultCollector);

            runner.run();
        }
        System.out.println(resultCollector.toReport());
        //new
    }
}
