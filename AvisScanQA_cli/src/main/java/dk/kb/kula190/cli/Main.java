package dk.kb.kula190.cli;

import dk.kb.kula190.ResultCollector;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class Main {
    
    public static void main(String[] args) throws IOException, URISyntaxException {
    
        //TODO get persistReport
        AvisScanQATool tool = new AvisScanQATool(true);
    
        ResultCollector resultCollector = tool.check(Path.of(args[0]).toAbsolutePath());
        
        System.exit(resultCollector.getFailures().size());
    }
    
}
