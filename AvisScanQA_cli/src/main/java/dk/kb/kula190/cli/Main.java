package dk.kb.kula190.cli;

import dk.kb.kula190.BasicRunnableComponent;
import dk.kb.kula190.Batch;
import dk.kb.kula190.DatabaseRegister;
import dk.kb.kula190.DecoratedRunnableComponent;
import dk.kb.kula190.MultiThreadedRunnableComponent;
import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.checkers.batchcheckers.MetsChecker;
import dk.kb.kula190.checkers.batchcheckers.MetsSplitter;
import dk.kb.kula190.checkers.editioncheckers.NoMissingMiddlePagesChecker;
import dk.kb.kula190.checkers.filecheckers.ChecksumChecker;
import dk.kb.kula190.checkers.filecheckers.FileNamingChecker;
import dk.kb.kula190.checkers.filecheckers.XmlSchemaChecker;
import dk.kb.kula190.checkers.filecheckers.tiff.TiffAnalyzerExiv2;
import dk.kb.kula190.checkers.filecheckers.tiff.TiffAnalyzerImageMagick;
import dk.kb.kula190.checkers.filecheckers.tiff.TiffCheckerExiv2;
import dk.kb.kula190.checkers.filecheckers.tiff.TiffCheckerImageMagick;
import dk.kb.kula190.checkers.pagecheckers.PageStructureChecker;
import dk.kb.kula190.checkers.pagecheckers.XpathPageChecker;
import dk.kb.kula190.generated.Failure;
import dk.kb.kula190.iterators.eventhandlers.TreeEventHandler;
import dk.kb.util.yaml.YAML;
import org.postgresql.Driver;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;

public class Main {
    
    public static void main(String[] args) throws Exception {
        
        Path batchPath = Path.of(args[0]).toAbsolutePath();
        Batch batch = new Batch(batchPath.getFileName().toString(), batchPath);
        
        ResultCollector simpleResultCollector = runSimpleChecks(batch);
        
        //TODO merge result coleectors
        System.out.println(simpleResultCollector.toReport());
        
        if (simpleResultCollector.isSuccess()) {
            ResultCollector resultCollector = runChecks(batch);
            
            System.out.println(resultCollector.toReport());
            
            
            ResultCollector dbResultCollector = registerResultInDB(batch, resultCollector);
            
            
            System.out.println(dbResultCollector.toReport());
        }
        
        System.exit(0);
    }
    
    private static ResultCollector runSimpleChecks(Batch batch) throws Exception {
        BasicRunnableComponent component =
                new BasicRunnableComponent() {
                    //TODO Why both override and functional interface? Cleanup this mess
                    @Override
                    protected List<TreeEventHandler> getCheckers(ResultCollector resultCollector) {
                        return List.of(
                                //Simple Checkers
                                new ChecksumChecker(resultCollector),
                                new FileNamingChecker(resultCollector)
                                      );
                    }
                };
        
        
        ResultCollector resultCollector = component.doWorkOnItem(batch);
        return resultCollector;
    }
    
    private static ResultCollector registerResultInDB(Batch batch, ResultCollector resultCollector) throws Exception {
        final List<Failure> failures = resultCollector.getFailures();
        File configFolder = new File(Thread.currentThread()
                                           .getContextClassLoader()
                                           .getResource("AvisScanQA_cli-behaviour.yaml")
                                           .toURI()).getParentFile();
        YAML dbConfig = YAML.resolveLayeredConfigs(configFolder.getAbsolutePath() + "/AvisScanQA_cli-*.yaml");
        
        
        DecoratedRunnableComponent databaseComponent = new DecoratedRunnableComponent() {
            @Override
            protected List<TreeEventHandler> getCheckers(ResultCollector resultCollector) {
                return List.of(
                        
                        new DatabaseRegister(resultCollector,
                                             new Driver(),
                                             dbConfig.getString("jdbc.jdbc-connection-string"),
                                             dbConfig.getString("jdbc.jdbc-user"),
                                             dbConfig.getString("jdbc.jdbc-password"),
                                             dbConfig.getString("states.initial-batch-state"),
                                             dbConfig.getString("states.finished-batch-state"),
                                             failures)
                              );
            }
            
        };
        ResultCollector dbResultCollector = databaseComponent.doWorkOnItem(batch);
        return dbResultCollector;
    }
    
    private static ResultCollector runChecks(Batch batch) throws Exception {
        //TODO configurable number of threads
        DecoratedRunnableComponent component = new MultiThreadedRunnableComponent(Executors.newFixedThreadPool(4)) {
            @Override
            protected List<TreeEventHandler> getCheckers(ResultCollector resultCollector) {
                return List.of(
                        new TiffAnalyzerExiv2(resultCollector),
                        new TiffCheckerExiv2(resultCollector),
                        
                        new TiffAnalyzerImageMagick(resultCollector),
                        new TiffCheckerImageMagick(resultCollector),
                        
                        new MetsSplitter(resultCollector),
                        new MetsChecker(resultCollector),
                        
                        //Per file- checkers
                        new XmlSchemaChecker(resultCollector),
                        
                        //CrossCheckers
                        new XpathPageChecker(resultCollector),
                        new NoMissingMiddlePagesChecker(resultCollector),
                        new PageStructureChecker(resultCollector)
                              );
                
            }
        };
        
        
        ResultCollector resultCollector = component.doWorkOnItem(batch);
        return resultCollector;
    }
    
    
}
