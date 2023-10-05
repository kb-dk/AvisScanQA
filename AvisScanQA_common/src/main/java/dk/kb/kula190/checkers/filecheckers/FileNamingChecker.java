package dk.kb.kula190.checkers.filecheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.generated.FailureType;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.NodeBeginsParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.DefaultTreeEventHandler;
import dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils;
import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileNamingChecker extends DefaultTreeEventHandler {

    private final DateTimeFormatter localDateFormatter;


    private String batchName;
    private Path batchFolder;
    private String newspaperName;
    private Pattern fileNamePattern;
    private LocalDate batchStartDate;
    private LocalDate batchEndDate;


    public FileNamingChecker(ResultCollector resultCollector) {
        super(resultCollector);

        localDateFormatter = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4)
                                                           .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                                                           .appendValue(ChronoField.DAY_OF_MONTH, 2)
                                                           .toFormatter();
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {

        String folderName = EventHandlerUtils.lastName(event.getName());
        if (batchName == null) {
            batchFolder = Path.of(event.getLocation());
            batchName = folderName;
            final String[] batchNameSplits = batchName.split("_", 5);
            newspaperName = batchNameSplits[0];
            fileNamePattern = Pattern.compile(Pattern.quote(newspaperName) + "_(\\d{4,8})_udg\\d{2}_[^_]+_\\d{4}");
            batchStartDate = LocalDate.parse(batchNameSplits[1], localDateFormatter);
            batchEndDate = LocalDate.parse(batchNameSplits[2], localDateFormatter);
            return;
        }

        String parentName = new File(event.getLocation()).getParentFile().getName();


        checkEquals(event,
                    FailureType.FILE_STRUCTURE_ERROR,
                    "Appendix H – File structure: Parent dir {actual} should always be batch dir {expected}, i.e. " +
                    "only one level of " +
                    "folders",
                    parentName,
                    batchName);


        //Only ALTO, METS, MIX, MODS, PDF, TIFF, JPEG allowed here
        checkInSet(event,
                   FailureType.FILE_STRUCTURE_ERROR,
                   "Appendix H – File structure: Folder name {0}/{actual} must be in one of {set}",
                   folderName,
                   Set.of("ALTO", "METS", "MIX", "MODS", "PDF", "TIFF", "Unmatched","JPEG"),
                   batchFolder);
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) throws IOException {
        final File file = new File(event.getLocation());
        String fileName = file.getName();
        File parent = file.getParentFile();
        String parentName = file.getParentFile().getName();

        String extension = EventHandlerUtils.getExtension(fileName);
        String nameWithoutExtension = EventHandlerUtils.removeExtension(fileName);
        if(extension.equals("jpeg") || extension.equals("jpg")){
            DecoratedAttributeParsingEvent.addJpegPath(nameWithoutExtension,event.getLocation());
        }
        if(fileOrFolderToIgnore(file)){return;}
        checkExtensionMatchFolder(event, parent, extension);
        checkNameMachPattern(event, extension, nameWithoutExtension);



    }
private Boolean fileOrFolderToIgnore(File file){

        if(file.getParentFile().getName().equals("Unmatched")){return true;}
        else if (EventHandlerUtils.getExtension(file.getName()).equals("jpeg")) {return true;}
        else if (EventHandlerUtils.getExtension(file.getName()).equals("jpg")) {return true;}
        else if (file.getName().equals(".XnViewSort")) {return true;}
        else if (file.getName().equals("Thumbs.db")) {return true;}
    return EventHandlerUtils.getExtension(file.getName()).equals("md5");

}
    private void checkNameMachPattern(AttributeParsingEvent event, String extension, String nameWithoutExtension) {
        //modersmaalet_19060701_udg01_MODERSMAALETS Søndagsblad_0003.md5
        switch (extension) {
            case "mods", "mets" -> checkEquals(event,
                                               FailureType.FILE_STRUCTURE_ERROR,
                                               "Appendix H – File structure: MODS/METS file '{actual}' must have same" +
                                               " name as batch '{expected}'",
                                               nameWithoutExtension,
                                               batchName);
            case "mix", "pdf", "tif", "tiff", "alto" -> {

                Matcher matcher = checkRegExp(event,
                                              FailureType.FILE_STRUCTURE_ERROR,
                                              "Appendix B – File names: Page-file file {actual} must have same name " +
                                              "as batch {expected}",
                                              nameWithoutExtension,
                                              fileNamePattern);
                if (matcher.matches()) {
                    LocalDate date;
                    if (matcher.group(1).length() == 4){
                        date = LocalDate.parse(matcher.group(1)+String.format("%02d",batchStartDate.getMonthValue())+String.format("%02d",batchStartDate.getDayOfMonth()), localDateFormatter);
                    }else{
                        date = LocalDate.parse(matcher.group(1), localDateFormatter);
                    }

                    checkBetween(event,
                                 FailureType.FILE_STRUCTURE_ERROR,
                                 date,
                                 batchStartDate,
                                 batchEndDate,
                                 "Appendix B – File names: Page have date {actual} outside of batch date interval " +
                                 "{requiredMin} - {requiredMax}");
                }

            }
            default -> addFailure(event, FailureType.FILE_STRUCTURE_ERROR,
                                  "Extension {0} is not expected here", extension);
        }
    }

    private void checkExtensionMatchFolder(AttributeParsingEvent event, File parentName, String extension) {
        switch (parentName.getName()) {
            case "ALTO" -> checkInSet(event,
                                      FailureType.FILE_STRUCTURE_ERROR,
                                      "Appendix H – File structure: File in folder {0}"
                                      + " must have one of these extensions "
                                      + "{expected} but has {actual}",
                                      extension,
                                      Set.of("alto", "alto.xml"),
                                      parentName);
            case "METS" -> checkInSet(event,
                                      FailureType.FILE_STRUCTURE_ERROR,
                                      "Appendix H – File structure: File in folder {0}"
                                      + " must have one of these extensions "
                                      + "{expected} but has {actual}",
                                      extension,
                                      Set.of("mets", "mets.xml"),
                                      parentName);
            case "MIX" -> checkInSet(event,
                                     FailureType.FILE_STRUCTURE_ERROR,
                                     "Appendix H – File structure: File in folder {0}"
                                     + " must have one of these extensions "
                                     + "{expected} but has {actual}",
                                     extension,
                                     Set.of("mix", "mix.xml"),
                                     parentName);
            case "MODS" -> checkInSet(event,
                                      FailureType.FILE_STRUCTURE_ERROR,
                                      "Appendix H – File structure: File in folder {0}"
                                      + " must have one of these extensions "
                                      + "{expected} but has {actual}",
                                      extension,
                                      Set.of("mods", "mods.xml"),
                                      parentName);
            case "PDF" -> checkInSet(event,
                                     FailureType.FILE_STRUCTURE_ERROR,
                                     "Appendix H – File structure: File in folder {0}"
                                     + " must have one of these extensions "
                                     + "{expected} but has {actual}",
                                     extension,
                                     Set.of("pdf"),
                                     parentName);
            case "TIFF" -> checkInSet(event,
                                                   FailureType.FILE_STRUCTURE_ERROR,
                                      "Appendix H – File structure: File in folder {0}"
                                      + " must have one of these extensions "
                                      + "{expected} but has {actual}",
                                                   extension,
                                                   Set.of("tif", "tiff"),
                                                   parentName);
            default -> addFailure(event,
                                  FailureType.FILE_STRUCTURE_ERROR,
                                  "Appendix H – File structure: File not allowed in folder {0}",
                                  batchFolder.getParent().relativize(Path.of(event.getLocation())).getParent());
        }
    }
}
