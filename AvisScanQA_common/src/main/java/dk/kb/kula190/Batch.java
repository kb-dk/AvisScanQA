package dk.kb.kula190;

import java.nio.file.Path;

public class Batch {
    
    private String fullID;
    private Path location;
    
    private boolean sections;
    
    public Batch(String fullID, Path location, boolean sections) {
        this.fullID   = fullID;
        this.location = location;
        this.sections = sections;
    }
    
    public String getFullID() {
        return fullID;
    }
    
    public Path getLocation() {
        return location;
    }
    
    public boolean isSections() {
        return sections;
    }
}
