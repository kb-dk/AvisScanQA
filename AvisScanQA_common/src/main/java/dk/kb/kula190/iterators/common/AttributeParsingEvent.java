package dk.kb.kula190.iterators.common;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class represents an attribute in the tree. An attribute is a node containing data directly, such as a
 * file system file or a fedora datastream.
 */
public abstract class AttributeParsingEvent extends ParsingEvent {
    
    public AttributeParsingEvent(String name) {
        super(name, ParsingEventType.Attribute, null);
    }
    
    public AttributeParsingEvent(String name, String location) {
        super(name, ParsingEventType.Attribute, location);
    }
    
    /**
     * Get the corresponding data
     *
     * @return the data
     * @throws IOException
     */
    public abstract InputStream getData() throws IOException;
    
    /**
     * Returns the checksum of the content or null
     *
     * @return the checksum
     * @throws IOException if there was a problem retrieving the data
     */
    public abstract String getChecksum() throws IOException;
    
}
