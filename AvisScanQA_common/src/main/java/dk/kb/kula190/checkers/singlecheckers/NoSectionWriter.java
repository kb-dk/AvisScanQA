package dk.kb.kula190.checkers.singlecheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedEventHandler;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedNodeParsingEvent;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.io.FilenameUtils.isExtension;
import static org.apache.commons.io.FilenameUtils.removeExtension;

//TODO NOT THREADSAFE
public class NoSectionWriter extends DecoratedEventHandler {
    
    private final DateTimeFormatter formatter;
    private final Path parentDir;
    
    private String batchName;
    /**
     * This map stores all the filename renames (without extension)
     * It is used to ensure that we rename all relevant names in the METS file
     */
    private Map<String, String> filenameMappings = new HashMap<>();
    //The mets data event
    private AttributeParsingEvent metsData;
    //This variable is part of the renumbering scheme. As pagenumbers previously ran inside the section, we now
    //need to run them per edition
    private Integer pageNumberLastSeen;
    
    
    public NoSectionWriter(ResultCollector resultCollector, Path parentDir) {
        super(resultCollector);
        this.parentDir = parentDir;
        formatter      = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4)
                                                       .appendValue(ChronoField.MONTH_OF_YEAR,
                                                                    2)
                                                       .appendValue(ChronoField.DAY_OF_MONTH,
                                                                    2)
                                                       .toFormatter();
    }
    
    @Override
    public void batchBegins(DecoratedNodeParsingEvent event,
                            String avis,
                            String roundTrip,
                            LocalDate startDate,
                            LocalDate endDate) {
        batchName = String.join("_", avis, startDate.format(formatter), endDate.format(formatter), "RT" + roundTrip);
        
        Path batchDir = Path.of(parentDir.toAbsolutePath().toString(), batchName);
        try {
            Files.createDirectories(batchDir);
            Files.createDirectories(batchDir.resolve("ALTO"));
            Files.createDirectories(batchDir.resolve("METS"));
            Files.createDirectories(batchDir.resolve("MIX"));
            Files.createDirectories(batchDir.resolve("MODS"));
            Files.createDirectories(batchDir.resolve("PDF"));
            Files.createDirectories(batchDir.resolve("TIFF"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void modsFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {
        Path newFile = Path.of(parentDir.toAbsolutePath().toString(),
                               batchName,
                               "MODS",
                               batchName + ".mods.xml");
        writeSectionlessXmlFile(event, newFile);
    }
    
    @Override
    public void metsFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         String roundTrip,
                         LocalDate startDate,
                         LocalDate endDate) throws IOException {
        //We cannot rewrite the mets here, as we need to wait until we have renamed the rest of the batch
        //Otherwise, we would not know which renames to perform
        metsData = event;
    }
    
    @Override
    public void batchEnds(DecoratedNodeParsingEvent event, String avis, String roundTrip, LocalDate startDate, LocalDate endDate)
            throws IOException {
        //Now that the batch have ended, it is time to rename the METS file
        Path metsFile = Path.of(parentDir.toAbsolutePath().toString(),
                                batchName,
                                "METS",
                                batchName + ".mets.xml");
        writeSectionlessGlobalXmlFile(metsData, metsFile);
    }
    
    @Override
    public void editionBegins(DecoratedNodeParsingEvent event, String avis, LocalDate editionDate, String editionName) {
        //When we start an edition, reset the page numbering
        pageNumberLastSeen = 0;
    }
    
    
    @Override
    public void pageBegins(DecoratedNodeParsingEvent event,
                           String editionName,
                           LocalDate editionDate,
                           String udgave,
                           String sectionName,
                           Integer pageNumber) {
        //The first section will continue with the same number scheme
        if (pageNumber < pageNumberLastSeen) {
            //If we suddently encounter a page with a lower number than the last seen, we know that we have reached
            // a new section.
            pageNumberLastSeen += 1;
        } else {
            pageNumberLastSeen = pageNumber;
        }
    }
    
    
    @Override
    public void mixFile(DecoratedAttributeParsingEvent event,
                        String avis,
                        LocalDate editionDate,
                        String udgave,
                        String sectionName,
                        Integer pageNumber) throws IOException {
        String folder = "MIX";
        String extension = ".mix.xml";
        writeSectionlessXmlFile(event, getNewfilePath(avis, editionDate, udgave, folder, extension));
    }
    
    
    @Override
    public void tiffFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        //TODO hardlink instead of copy
        String folder = "TIFF";
        String extension = ".tif";
        writeSectionlessBinaryFile(event, avis, editionDate, udgave, folder, extension);
    }
    
    @Override
    public void altoFile(DecoratedAttributeParsingEvent event,
                         String avis,
                         LocalDate editionDate,
                         String udgave,
                         String sectionName,
                         Integer pageNumber) throws IOException {
        String folder = "ALTO";
        String extension = ".alto.xml";
        writeSectionlessXmlFile(event, getNewfilePath(avis, editionDate, udgave, folder, extension));
    }
    
    @Override
    public void pdfFile(DecoratedAttributeParsingEvent event,
                        String avis,
                        LocalDate editionDate,
                        String udgave,
                        String sectionName,
                        Integer pageNumber) throws IOException {
        //TODO hardlink instead of copy
        String folder = "PDF";
        String extension = ".pdf";
        writeSectionlessBinaryFile(event, avis, editionDate, udgave, folder, extension);
    }
    
    private void writeSectionlessXmlFile(AttributeParsingEvent event, Path newfilePath) throws IOException {
        Path orig_file = Path.of(event.getLocation());
        String oldName = removeXmlExtension(orig_file.getFileName().toString());
        
        String newName = removeXmlExtension(newfilePath.getFileName().toString());
        
        filenameMappings.put(oldName, newName);
        
        Stream<String> lines = Files.lines(orig_file);
        List<String> replaced = lines.map(line -> line.replaceAll(Pattern.quote(oldName), newName))
                                     .collect(Collectors.toList());
        Files.write(newfilePath,
                    replaced,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        try (InputStream newContents = new FileInputStream(newfilePath.toFile())) {
            writeMD5File(newfilePath, DigestUtils.md5Hex(newContents));
        }
    }
    
    private void writeSectionlessGlobalXmlFile(AttributeParsingEvent event, Path newfilePath) throws IOException {
        Path orig_file = Path.of(event.getLocation());
        Stream<String> lines = Files.lines(orig_file);
        List<String> replaced = lines.map(line -> {
                                         for (Map.Entry<String, String> entry : filenameMappings.entrySet()) {
                                             line = line.replace(entry.getKey(), entry.getValue());
                                         }
                                         return line;
                                     })
                                     .collect(Collectors.toList());
        Files.write(newfilePath,
                    replaced,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        try (InputStream newContents = new FileInputStream(newfilePath.toFile())) {
            writeMD5File(newfilePath, DigestUtils.md5Hex(newContents));
        }
    }
    
    private void writeSectionlessBinaryFile(AttributeParsingEvent event,
                                            String avis,
                                            LocalDate editionDate,
                                            String udgave,
                                            String mix,
                                            String extension) throws IOException {
        
        
        Path newfilePath = Path.of(parentDir.toAbsolutePath().toString(),
                                   batchName,
                                   mix,
                                   String.join("_",
                                               avis,
                                               editionDate.format(formatter),
                                               udgave,
                                               String.format("%04d", pageNumberLastSeen)) + extension);
        Files.deleteIfExists(newfilePath);
        Files.createLink(newfilePath, Path.of(event.getLocation()));
        
        //try (InputStream data = event.getData();) {
        //    Files.copy(data, newfilePath);
        //}
        writeMD5File(newfilePath, event.getChecksum());
        
    }
    
    private void writeMD5File(Path newfilePath, String checksum) throws IOException {
        Files.writeString(Path.of(removeXmlExtension(newfilePath.toString()) + ".md5"),
                          checksum + "  " + newfilePath.getFileName().toString(),
                          StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }
    
    
    private Path getNewfilePath(String avis, LocalDate editionDate, String udgave, String mix, String extension) {
        return Path.of(parentDir.toAbsolutePath().toString(),
                       batchName,
                       mix,
                       String.join("_",
                                   avis,
                                   editionDate.format(formatter),
                                   udgave,
                                   String.format("%04d", pageNumberLastSeen)) + extension);
    }
    
    private String removeXmlExtension(String filename) {
        if (isExtension(filename, "xml")) {
            return removeXmlExtension(removeExtension(filename));
        } else {
            return removeExtension(filename);
        }
    }
    
    
}
