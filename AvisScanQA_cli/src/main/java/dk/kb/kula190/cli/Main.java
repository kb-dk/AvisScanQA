package dk.kb.kula190.cli;

import dk.kb.kula190.Batch;
import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.RunnableComponent;
import dk.kb.kula190.checkers.ChecksumChecker;
import dk.kb.kula190.checkers.crosscheckers.NoMissingMiddlePagesChecker;
import dk.kb.kula190.checkers.crosscheckers.PageStructureChecker;
import dk.kb.kula190.checkers.singlecheckers.XmlSchemaChecker;
import dk.kb.kula190.iterators.eventhandlers.TreeEventHandler;

import java.nio.file.Path;
import java.util.List;

public class Main {
    
    public static void main(String[] args) throws Exception {
    
        RunnableComponent component = new RunnableComponent() {
            @Override
            protected List<TreeEventHandler> getCheckers(ResultCollector resultCollector) {
                return List.of(
                        new ChecksumChecker(resultCollector),
                        new NoMissingMiddlePagesChecker(resultCollector),
                        new PageStructureChecker(resultCollector),
                        new XmlSchemaChecker(resultCollector));
            }
        };
    
        Path batchPath = Path.of(args[0]).toAbsolutePath();
        Batch batch = new Batch(batchPath.getFileName().toString(), batchPath);
        ResultCollector resultCollector = component.doWorkOnItem(batch);
    
        System.out.println(resultCollector.toReport());
    }
    

}
