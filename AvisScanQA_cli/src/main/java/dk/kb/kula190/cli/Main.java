package dk.kb.kula190.cli;

import dk.kb.kula190.ResultCollector;
import dk.kb.util.yaml.YAML;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class Main {
    
    public static void main(String[] args) throws IOException, URISyntaxException {
    
        File configFolder = new File(Thread.currentThread()
                                           .getContextClassLoader()
                                           .getResource("AvisScanQA_cli-behaviour.yaml")
                                           .toURI()).getParentFile();
        YAML config = YAML.resolveLayeredConfigs(configFolder.getAbsolutePath() + "/AvisScanQA_cli-*.yaml");
        
        //TODO get persistReport
        AvisScanQATool tool = new AvisScanQATool(config);
    
        ResultCollector resultCollector = tool.check(Path.of(args[0]).toAbsolutePath());
        
        System.exit(resultCollector.getFailures().size());
    }
    
}
