package dk.kb.kula190.checkers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

//TODO NOT THREADSAFE
public class PageStructureChecker extends DecoratedEventHandler {
    private ThreadLocal<Set<String>> types = new ThreadLocal<>();
    public PageStructureChecker(ResultCollector resultCollector) {
        super(resultCollector);
        types.set(new HashSet<>());
    }

    @Override
    public void pageBegins(ParsingEvent event, String avis, LocalDate editionDate, String udgave, String sectionName, Integer pageNumber) {
        //Checkes that each page consists of MIX ALTO TIFF
        types.get().clear();
    }

    @Override
    public void pageEnds(ParsingEvent event, String avis, LocalDate editionDate, String udgave, String sectionName, Integer pageNumber) {

        if(!types.get().containsAll(Set.of("MIX","ALTO","TIFF"))){
            getResultCollector().addFailure(event.getName(),
                    "Missing files per page",
                    this.getClass().getSimpleName(),
                    "Must contain mix, alto and tiff files",
                    types.get().stream().map(x -> "" + x).collect(Collectors.joining(",")));
        }
    }

    @Override
    public void mixFile(AttributeParsingEvent event, String avis, LocalDate editionDate, String udgave, String sectionName, Integer pageNumber) throws IOException {
        types.get().add("MIX");
    }

    @Override
    public void altoFile(AttributeParsingEvent event, String avis, LocalDate editionDate, String udgave, String sectionName, Integer pageNumber) throws IOException {
        types.get().add("ALTO");
    }

    @Override
    public void tiffFile(AttributeParsingEvent event, String avis, LocalDate editionDate, String udgave, String sectionName, Integer pageNumber) throws IOException {
        types.get().add("TIFF");
    }
}
