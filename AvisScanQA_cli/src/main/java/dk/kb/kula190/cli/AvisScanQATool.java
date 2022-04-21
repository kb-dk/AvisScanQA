package dk.kb.kula190.cli;

import dk.kb.kula190.BasicRunnableComponent;
import dk.kb.kula190.Batch;
import dk.kb.kula190.DatabaseRegister;
import dk.kb.kula190.DecoratedRunnableComponent;
import dk.kb.kula190.EmailSender;
import dk.kb.kula190.MultiThreadedRunnableComponent;
import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.checkers.batchcheckers.MetsChecker;
import dk.kb.kula190.checkers.batchcheckers.MetsSplitter;
import dk.kb.kula190.checkers.editioncheckers.NoMissingMiddlePagesChecker;
import dk.kb.kula190.checkers.filecheckers.ChecksumChecker;
import dk.kb.kula190.checkers.filecheckers.FileNamingChecker;
import dk.kb.kula190.checkers.filecheckers.ProgressLogger;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class AvisScanQATool {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final YAML config;
    
    private final String checksumFile;
    private final String acknowledgmentFile;
    private final List<String> filesToIgnore;
    
    public AvisScanQATool(YAML config, String checksumFile, String acknowledgmentFile, List<String> filesToIgnore) {
        this.config             = config;
        this.checksumFile       = checksumFile;
        this.acknowledgmentFile = acknowledgmentFile;
        this.filesToIgnore      = filesToIgnore;
    }
    
    public ResultCollector check(Path batchPath) throws IOException {
        
        Batch batch = new Batch(batchPath.getFileName().toString(), batchPath);
        
        ResultCollector resultCollector = new ResultCollector(getClass().getSimpleName(),
                                                              getClass().getPackage().getImplementationVersion(),
                                                              null);
        
        try {
            log.info("Initial filenameChecking of batch {}", batch.getFullID());
            //Perform basic checks
            final BasicRunnableComponent
                    basicRunnableComponent
                    = new BasicRunnableComponent(r -> List.of(new FileNamingChecker(r)), checksumFile, filesToIgnore);
            
            basicRunnableComponent.doWorkOnItem(batch, resultCollector);
            
        } catch (Exception e) {
            resultCollector.addExceptionalFailure(e);
        }
        
        //If the basic checks worked, proceed
        if (resultCollector.isSuccess()) {
            try {
                final int nThreads = config.getInteger("iterator.numThreads",
                                                       Runtime.getRuntime().availableProcessors());
                log.info("Starting full checks of batch {} with {} threads", batch.getFullID(), nThreads);
                
                DecoratedRunnableComponent component = new MultiThreadedRunnableComponent(Executors.newFixedThreadPool(
                        nThreads), checkerFactory(), checksumFile, filesToIgnore);
                
                component.doWorkOnItem(batch, resultCollector);
                
            } catch (Exception e) {
                resultCollector.addExceptionalFailure(e);
            }
        } else {
            log.error("Failed basic checks:, so no point in performing advanced checks ");
        }
        if (config.getBoolean("jdbc.enabled")) {
            log.info("Registering results of QA on batch {} in database", batch.getFullID());
            registerResultInDB(batch, resultCollector, config);
    
    
            Properties emailConfig;
            if (useYamlConfig) {
                emailConfig = dk.kb.util.yaml.YAMLUtils.toProperties(config.getSubMap("mail.smtp", true))
            } else {//Hardcoded email config
                emailConfig = new Properties();
    
                emailConfig.setProperty("smtp.auth", "false");
                emailConfig.setProperty("smtp.starttls.enable", "true");
                emailConfig.setProperty("smtp.host", "smtp.statsbiblioteket.dk");
                emailConfig.setProperty("smtp.port", "25");
    
            }
            EmailSender.newInstance()
                       .to(recipient)
                       .from(from)
                       .cc(cc)
                       .bcc(bcc)
                       .subject(subject)
                       .bodyText(bodyText)
                       .attachment(pdfFile)
                       .send(emailConfig);
        }
        
        log.info("All checks done, returning");
        return resultCollector;
    }
    
    
    private void registerResultInDB(Batch batch, ResultCollector resultCollector, YAML config) throws IOException {
        final List<Failure> failures = resultCollector.getFailures();
        
        DecoratedRunnableComponent databaseComponent = new DecoratedRunnableComponent(
                rc -> List.of(new DatabaseRegister(rc,
                                                   new Driver(),
                                                   config.getString("jdbc.jdbc-connection-string"),
                                                   config.getString("jdbc.jdbc-user"),
                                                   config.getString("jdbc.jdbc-password"),
                                                   config.getString("states.initial-batch-state"),
                                                   config.getString("states.finished-batch-state"),
                                                   acknowledgmentFile,
                                                   failures)),
                checksumFile,
                filesToIgnore);
        databaseComponent.doWorkOnItem(batch, resultCollector);
        
    }
    
    private Function<ResultCollector, List<TreeEventHandler>> checkerFactory() {
        //TODO ensure this is the current and correct list of checkers
        return resultCollector -> List.of(
                
                //BatchCheckers
                new MetsSplitter(resultCollector), new MetsChecker(resultCollector),
                
                //EditionCheckers
                new NoMissingMiddlePagesChecker(resultCollector),
                
                //PageCheckers
                new XpathPageChecker(resultCollector), new PageStructureChecker(resultCollector),
                
                //FileCheckers
                new ChecksumChecker(resultCollector), new XmlSchemaChecker(resultCollector),
                
                //Tiff Checkers - Exiv2
                new TiffAnalyzerExiv2(resultCollector), new TiffCheckerExiv2(resultCollector),
                
                //Tiff Checkers - ImageMagick
                new TiffAnalyzerImageMagick(resultCollector), new TiffCheckerImageMagick(resultCollector),
                
                new ProgressLogger(resultCollector));
    }
    
}
