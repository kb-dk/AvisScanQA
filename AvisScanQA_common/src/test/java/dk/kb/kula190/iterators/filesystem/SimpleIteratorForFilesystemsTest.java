package dk.kb.kula190.iterators.filesystem;


import dk.kb.kula190.iterators.AbstractTests;
import dk.kb.kula190.iterators.common.TreeIterator;
import dk.kb.kula190.iterators.filesystem.transparent.TransparintingFileSystemIterator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.List;


public class SimpleIteratorForFilesystemsTest extends AbstractTests {
    
    private TreeIterator iterator;
    
    @Override
    public TreeIterator getIterator() throws URISyntaxException {
        if (iterator == null) {
            //File file = new File(Thread.currentThread().getContextClassLoader().getResource("batch").toURI());
            File file = new File("/home/pabr/Projects/AvisScanQA/data/modersmaalet_19060701_19061231_RT1");
            
            System.out.println(file);
            iterator = new TransparintingFileSystemIterator(file,
                                                            file.getParentFile(),
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
                    
                                                            "\\.[^_]+$",
                                                            ".md5");
        }
        return iterator;
        
    }
    
    
    @Test
    public void testIterator() throws Exception {
        try (PrintStream out = new PrintStream("batch.xml")) {
            super.testIterator(out, null);
        }
    }
    
    @Test
    @Disabled
    public void testIteratorWithSkipping() throws Exception {
        try (PrintStream out = new PrintStream("batch.xml")) {
            super.testIteratorWithSkipping(out, null);
        }
    }
}
