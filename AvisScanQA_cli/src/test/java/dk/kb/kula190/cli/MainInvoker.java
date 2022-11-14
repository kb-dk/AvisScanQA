package dk.kb.kula190.cli;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class MainInvoker {
    
    
    private static final File
            specificBatch
            = new File(System.getenv("HOME") + "/AvisScanQA_cli/lastTIme/data2/freja1849_18491022_18501231_RT1");
    
    public static void main(String[] args) throws IOException, URISyntaxException {
        Main.main(specificBatch.getAbsolutePath());
    }
}
