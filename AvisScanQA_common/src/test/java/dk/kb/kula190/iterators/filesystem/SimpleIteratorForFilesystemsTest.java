package dk.kb.kula190.iterators.filesystem;


import dk.kb.kula190.iterators.AbstractTests;
import dk.kb.kula190.iterators.common.TreeIterator;
import dk.kb.kula190.iterators.filesystem.transparent.TransparintingFileSystemIterator;
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
    public void testIterator() throws Exception {
        try (PrintStream out = new PrintStream("batch.xml")) {
            super.testIterator(out, null);
        }
    }
    
    @Test
    public void testIteratorWithSkipping() throws Exception {
        try (PrintStream out = new PrintStream("batch.xml")) {
            super.testIteratorWithSkipping(out, null);
        }
    }
}
