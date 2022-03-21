package dk.kb.kula190.iterators.eventhandlers;

import dk.kb.kula190.Batch;
import dk.kb.kula190.DatabaseRegister;
import dk.kb.kula190.MultiThreadedRunnableComponent;
import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.DecoratedRunnableComponent;
import dk.kb.kula190.checkers.editioncheckers.NoMissingMiddlePagesChecker;
import dk.kb.kula190.checkers.pagecheckers.PageStructureChecker;
import dk.kb.kula190.checkers.pagecheckers.XpathPageChecker;
import dk.kb.kula190.checkers.filecheckers.tiff.TiffAnalyzerImageMagick;
import dk.kb.kula190.checkers.filecheckers.tiff.TiffCheckerImageMagick;
import dk.kb.kula190.checkers.filecheckers.XmlSchemaChecker;
import dk.kb.kula190.checkers.filecheckers.XpathAltoChecker;
import dk.kb.kula190.checkers.filecheckers.XpathMixChecker;
import dk.kb.kula190.generated.Failure;
import dk.kb.util.yaml.YAML;
import org.junit.jupiter.api.Test;
import org.postgresql.Driver;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;

class DatabaseEventRunnerTest {
    
    private final File
            specificBatch
            = new File(System.getenv("HOME") + "/Projects/AvisScanQA/data/orig/modersmaalet_19060701_19060709_RT1");
    
    
    @Test
    void run() throws Exception {
    
        Path batchPath = specificBatch.toPath().toAbsolutePath();
        Batch batch = new Batch(batchPath.getFileName().toString(), batchPath);
    
    
        DecoratedRunnableComponent component = new MultiThreadedRunnableComponent(Executors.newFixedThreadPool(4)) {
            @Override
            protected List<TreeEventHandler> getCheckers(ResultCollector resultCollector) {
                return List.of(
                    
                        //Per file- checkers
                        new XmlSchemaChecker(resultCollector),
                        new TiffAnalyzerImageMagick(resultCollector),
                        new TiffCheckerImageMagick(resultCollector),
                        new XpathAltoChecker(resultCollector),
                        new XpathMixChecker(resultCollector),
                    
                        //CrossCheckers
                        new XpathPageChecker(resultCollector),
                        new NoMissingMiddlePagesChecker(resultCollector),
                        new PageStructureChecker(resultCollector)
            
            
                              );
            }
        
        };
    
        ResultCollector resultCollector = component.doWorkOnItem(batch);
    
        System.out.println(resultCollector.toReport());
    
        final List<Failure> failures = resultCollector.getFailures();
        File configFolder = new File(Thread.currentThread()
                                   .getContextClassLoader()
                                   .getResource("dbconfig-behaviour.yaml")
                                   .toURI()).getParentFile();
        YAML dbConfig = YAML.resolveLayeredConfigs(configFolder.getAbsolutePath() + "/dbconfig-*.yaml");
        
        
        DecoratedRunnableComponent databaseComponent = new DecoratedRunnableComponent() {
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
    
    
    }
}
