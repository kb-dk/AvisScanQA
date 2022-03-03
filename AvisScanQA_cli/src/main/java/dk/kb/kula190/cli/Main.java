package dk.kb.kula190.cli;

import dk.kb.kula190.Batch;
import dk.kb.kula190.MultiThreadedRunnableComponent;
import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.RunnableComponent;
import dk.kb.kula190.checkers.DatabaseRegister;
import dk.kb.kula190.checkers.crosscheckers.NoMissingMiddlePagesChecker;
import dk.kb.kula190.checkers.crosscheckers.PageStructureChecker;
import dk.kb.kula190.checkers.crosscheckers.XpathCrossChecker;
import dk.kb.kula190.checkers.singlecheckers.TiffAnalyzerImageMagick;
import dk.kb.kula190.checkers.singlecheckers.TiffCheckerImageMagick;
import dk.kb.kula190.checkers.singlecheckers.XmlSchemaChecker;
import dk.kb.kula190.checkers.singlecheckers.XpathAltoChecker;
import dk.kb.kula190.checkers.singlecheckers.XpathMixChecker;
import dk.kb.kula190.generated.Failure;
import dk.kb.kula190.iterators.eventhandlers.TreeEventHandler;
import dk.kb.util.yaml.YAML;
import org.postgresql.Driver;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class Main {
    
    public static void main(String[] args) throws Exception {
    
        RunnableComponent component = new MultiThreadedRunnableComponent() {
            @Override
            protected List<TreeEventHandler> getCheckers(ResultCollector resultCollector) {
                return List.of(
                        //Simple Checkers
//                        new ChecksumChecker(resultCollector),
        
                        //Per file- checkers
                        new XmlSchemaChecker(resultCollector),
                        new TiffAnalyzerImageMagick(resultCollector),
                        new TiffCheckerImageMagick(resultCollector),
                        new XpathAltoChecker(resultCollector),
                        new XpathMixChecker(resultCollector),
        
                        //CrossCheckers
                        new XpathCrossChecker(resultCollector),
                        new NoMissingMiddlePagesChecker(resultCollector),
                        new PageStructureChecker(resultCollector));
            }
        };
    
        Path batchPath = Path.of(args[0]).toAbsolutePath();
        Batch batch = new Batch(batchPath.getFileName().toString(), batchPath);
        ResultCollector resultCollector = component.doWorkOnItem(batch);
    
    
        System.out.println(resultCollector.toReport());
        
        
        
        final List<Failure> failures = resultCollector.getFailures();
        File configFolder = new File(Thread.currentThread()
                                           .getContextClassLoader()
                                           .getResource("dbconfig-behaviour.yaml")
                                           .toURI()).getParentFile();
        YAML dbConfig = YAML.resolveLayeredConfigs(configFolder.getAbsolutePath() + "/dbconfig-*.yaml");
    
    
        RunnableComponent databaseComponent = new RunnableComponent() {
            @Override
            protected List<TreeEventHandler> getCheckers(ResultCollector resultCollector) {
                return List.of(
                    
                        new DatabaseRegister(resultCollector,
                                             new Driver(),
                                             dbConfig.getString("jdbc.jdbc-connection-string"),
                                             dbConfig.getString("jdbc.jdbc-user"),
                                             dbConfig.getString("jdbc.jdbc-password"),
                                             failures)
                              );
            }
        
        };
        ResultCollector dbResultCollector = databaseComponent.doWorkOnItem(batch);
    
    
        System.out.println(dbResultCollector.toReport());
        
        System.exit(0);
    }
    

}
