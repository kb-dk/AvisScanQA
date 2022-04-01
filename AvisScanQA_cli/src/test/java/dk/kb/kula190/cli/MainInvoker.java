package dk.kb.kula190.cli;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class MainInvoker {
    
    
    private static final File
            specificBatch
            = new File(System.getenv("HOME") + "/Projects/AvisScanQA/data/orig/modersmaalet_19060701_19060709_RT1");
    
    public static void main(String[] args) throws IOException, URISyntaxException {
        Main.main(specificBatch.getAbsolutePath());
    }
}
