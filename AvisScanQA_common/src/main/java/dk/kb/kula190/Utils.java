package dk.kb.kula190;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.HashSet;
import java.util.Set;

public class Utils {
    public static Node asSeparateXML(Node metadataMods) throws ParserConfigurationException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node mods = document.appendChild(document.adoptNode(metadataMods));
        return document.getDocumentElement();
    }
    
    public static Set<String> fromAnotInB(Set<String> altoFilesFromMets, Set<String> altoFilesVisited) {
        HashSet<String> diff = new HashSet<>(altoFilesFromMets);
        diff.removeAll(altoFilesVisited);
        return diff;
    }
}
