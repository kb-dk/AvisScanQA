package dk.kb.kula190.iterators.eventhandlers.decorating;

import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.FilenameUtils.isExtension;

public class DecoratedAttributeParsingEvent extends AttributeParsingEvent implements DecoratedParsingEvent {
    
    private final AttributeParsingEvent delegate;
    private final String avis;
    private final String roundTrip;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDate editionDate;
    private final String udgave;
    private final String sectionName;
    private final Integer pageNumber;
    private static final Map<String,String> jpegPaths = new HashMap<>();
    private static final Map<String,String> altoPaths = new HashMap<>();

    public DecoratedAttributeParsingEvent(AttributeParsingEvent delegate) {
        super(delegate.getName(), delegate.getLocation());
    
        //modersmaalet_19060701_udg01_MODERSMAALETS Søndagsblad_0001.mix.xml
        //modersmaalet_19060701_19061231_RT1.mods.xml
    
        final String lastName = EventHandlerUtils.removeExtension(EventHandlerUtils.lastName(delegate.getName()));
        String avis = null;
        LocalDate editionDate = null;
        String udgave = null;
        String sectionName = null;
        Integer pageNumber = null;
    
        String batch;
        if (lastName.matches(".*_RT\\d+$")) {
            //batchlike
            //batch: modersmaalet_19060701_19061231_RT1
            //mets/mods:  modersmaalet_19060701_19061231_RT1
            batch = lastName;
        } else { //page/section/edition-like
            batch = EventHandlerUtils.firstName(delegate.getName());
        
        
            //section: modersmaalet_19060706_udg01_1.sektion
            //edition: modersmaalet_19060706_udg01
            //page: modersmaalet_19060706_udg01_1.sektion_001
            String[] splits = lastName.split("_", 5);
            avis        = splits[0];
            if (splits[1].length() == 4){
                splits[1] = splits[1]+"0101";
            }
            editionDate = LocalDate.parse(splits[1], EventHandlerUtils.dateFormatter);
            udgave      = splits[2];
            if (splits.length > 3) {
                sectionName = splits[3];
            }
            if (splits.length > 4) {
                pageNumber = Integer.parseInt(splits[4]);
            }
        }
    
    
        String[] splits2 = batch.split("_", 4);
        LocalDate startDate = LocalDate.parse(splits2[1], EventHandlerUtils.dateFormatter);
        LocalDate endDate = LocalDate.parse(splits2[2], EventHandlerUtils.dateFormatter);
        String avis2 = splits2[0];
        String roundTrip = splits2[3].replaceFirst("^RT", "");
    
        this.delegate    = delegate;
        this.avis        = Optional.ofNullable(avis).orElse(avis2);
        this.roundTrip   = roundTrip;
        this.startDate   = startDate;
        this.endDate     = endDate;
        this.editionDate = editionDate;
        this.udgave      = udgave;
        this.sectionName = sectionName;
        this.pageNumber  = pageNumber;
    }
    
    @Override
    public InputStream getData() throws IOException {
        return delegate.getData();
    }
    
    @Override
    public String getChecksum() throws IOException {
        return delegate.getChecksum();
    }
    
    @Override
    public String getAvis() {
        return avis;
    }
    
    @Override
    public String getRoundTrip() {
        return roundTrip;
    }
    
    @Override
    public LocalDate getStartDate() {
        return startDate;
    }
    @Override
    public LocalDate getEndDate() {
        return endDate;
    }
    @Override
    public LocalDate getEditionDate() {
        return editionDate;
    }
    @Override
    public String getUdgave() {
        return udgave;
    }
    @Override
    public String getSectionName() {
        return sectionName;
    }
    
    @Override
    public Integer getPageNumber() {
        return pageNumber;
    }

    public static String getJpegPath(String filename){
        return jpegPaths.get(filename);
    }
    public static String addJpegPath(String filename,String path){
        return jpegPaths.put(filename,path);
    }
    public static String getAltoPath(String filename){
        return altoPaths.get(filename);
    }
    public static String addAltoPath(String filename,String path){
        return altoPaths.put(filename,path);
    }
    public static Map<String,String> getJpegPaths(){
        return jpegPaths;
    }
    public static Map<String,String> getAltoPaths(){
        return altoPaths;
    }
    @Override
    public String toString() {
        return "DecoratedAttributeParsingEvent{" +
               "type=" + type +
               ", name='" + name + '\'' +
               ", location='" + location + '\'' +
               ", avis='" + avis + '\'' +
               ", roundTrip='" + roundTrip + '\'' +
               ", startDate=" + startDate +
               ", endDate=" + endDate +
               ", editionDate=" + editionDate +
               ", udgave='" + udgave + '\'' +
               ", sectionName='" + sectionName + '\'' +
               ", pageNumber=" + pageNumber +
               '}';
    }
}
