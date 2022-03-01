package dk.kb.kula190.checkers.singlecheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.InjectedAttributeParsingEvent;
import dk.kb.kula190.iterators.eventhandlers.EventHandlerUtils;
import dk.kb.kula190.iterators.eventhandlers.InjectingTreeEventHandler;
import dk.kb.util.xml.XML;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MetsSplitter extends InjectingTreeEventHandler {
    
    public static final String INJECTED_TYPE_MIX = "METS MIX";
    public static final String INJECTED_TYPE_METS = "METS REDUCED";
    
    private final ThreadLocal<Node> fileSec = new InheritableThreadLocal<>();
    private final ThreadLocal<Node> amdSec = new InheritableThreadLocal<>();
    
    public MetsSplitter(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    @Override
    public void handleAttribute(AttributeParsingEvent event) throws IOException {
        String extension = EventHandlerUtils.getExtension(event.getName());
        
        XPathSelector xpath = XpathUtils.createXPathSelector("mets", "http://www.loc.gov/METS/",
                                                             "dc", "http://purl.org/dc/elements/1.1/",
                                                             "mix", "http://purl.org/dc/elements/1.1/mix",
                                                             "premis", "http://purl.org/dc/elements/1.1/premis",
                                                             "xlink", "http://www.w3.org/1999/xlink");
        
        if (event instanceof InjectedAttributeParsingEvent) {
            // Do not handle our own injected events
            return;
        }
        if (extension.equals("mets")) {
            
            try (InputStream data = event.getData()) {
                Document metsDoc = XML.fromXML(data, true);
                
                amdSec.set(xpath.selectNode(metsDoc, "/mets:mets")
                                .removeChild(xpath.selectNode(metsDoc, "/mets:mets/mets:amdSec")));
                fileSec.set(xpath.selectNode(metsDoc, "/mets:mets/mets:fileSec"));
                metsDoc.normalizeDocument();
                pushEvent(event, INJECTED_TYPE_METS,
                          XML.domToString(metsDoc).getBytes(StandardCharsets.UTF_8));
                
            } catch (ParserConfigurationException | SAXException | TransformerException e) {
                throw new IOException("Failed to parse METS data from " + event.getLocation(), e);
            }
        } else if (extension.equals("tif")) {
            String filename = EventHandlerUtils.lastName(event.getName());
            Node fileSecDom = fileSec.get();
            Node amdSecDom = amdSec.get();

            String tifFileRef = "..\\TIFF\\" + filename;
            Node fileNode = xpath.selectNode(fileSecDom, "mets:fileGrp[@ID='TIFF']/"
                                                         + "mets:file[mets:FLocat/@xlink:href='" + tifFileRef + "']");
//            String id = fileNode.getAttributes().getNamedItem("ID").getNodeValue();
            String admid = fileNode.getAttributes().getNamedItem("ADMID").getNodeValue();
    
            
            Node techMD = xpath.selectNode(amdSecDom, ""
                                                + "mets:techMD[@ID='" + admid + "']/"
                                                + "mets:mdWrap/mets:xmlData/*");
            
           /* String altoID = xpath.selectString(metsDoc,
                                               "/mets:mets/"
                                               + "mets:structMap[@TYPE='physical']/"
                                               + "mets:div[@DMDID='DMD1']/"
                                               + "mets:div[@TYPE='Page'][mets:fptr/@FILEID='" + id + "']/"
                                               + "mets:fptr[@FILEID!='" + id + "']/"
                                               + "@FILEID");
            String altoFileRef = xpath.selectString(metsDoc,
                                                    "/mets:mets/mets:fileSec/mets:fileGrp[@ID='ALTO']/mets:file[@ID='"
                                                    + altoID
                                                    + "']/mets:FLocat/@xlink:href");*/
            try {
                pushEvent(event, INJECTED_TYPE_MIX, XML.domToString(techMD).getBytes(StandardCharsets.UTF_8));
            } catch (TransformerException e) {
                throw new IOException("Failed to extract MIX data from METS for file " + event.getLocation(), e);
            }
        }
    }
}
