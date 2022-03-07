package dk.kb.kula190.iterators.filesystem;


import dk.kb.kula190.iterators.common.AbstractIterator;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.DelegatingTreeIterator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Iterator for parsing a tree structure backed by a file system. Each iterator represents a node. A node corresponds
 * to a directory.
 */
public class SimpleIteratorForFilesystems extends AbstractIterator<File> {
    
    
    private String checksumRegexp;
    private String checksumExtension;
    
    /**
     * Construct an iterator rooted at a given directory
     *
     * @param dir               the directory at which to root the iterator.
     * @param checksumRegexp
     * @param checksumExtension
     */
    public SimpleIteratorForFilesystems(File dir, String checksumRegexp, String checksumExtension) {
        super(dir);
        this.checksumRegexp    = checksumRegexp;
        this.checksumExtension = checksumExtension;
    }
    
    @Override
    protected Iterator<? extends DelegatingTreeIterator> initializeChildrenIterator() {
        //The id attribute is the id of this node, ie. the File corresponding to the directory
        File[] children = id.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        Arrays.sort(children);
        ArrayList<DelegatingTreeIterator> result = new ArrayList<>(children.length);
        for (File child : children) {
            result.add(new SimpleIteratorForFilesystems(child, checksumRegexp, checksumExtension));
        }
        return result.iterator();
    }
    
    @Override
    protected Iterator<File> initilizeAttributeIterator() {
        List<File> attributes = new ArrayList<>(FileUtils.listFiles(id,
                                                                    new AndFileFilter(FileFileFilter.INSTANCE,
                                                                                      new NotFileFilter(
                                                                                              new WildcardFileFilter(
                                                                                                      "*"
                                                                                                      + checksumExtension))),
                                                                    null));
        Collections.sort(attributes);
        return attributes.iterator();
    }
    
    @Override
    protected AttributeParsingEvent makeAttributeEvent(File nodeID, File attributeID) {
        return new FileAttributeParsingEvent(attributeID.getPath(), attributeID, checksumRegexp, checksumExtension);
    }
    
    
    /**
     * The name of the directory is used as the Id of the node.
     *
     * @return the name of the directory.
     */
    @Override
    protected String getIdOfNode() {
        return id.getPath();
    }
    
    
}
