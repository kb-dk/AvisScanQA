package dk.kb.kula190.checkers.batchcheckers;

import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.checkers.pagecheckers.XpathPageChecker;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MetsSplitter extends InjectingTreeEventHandler {
    
    public static final String INJECTED_TYPE_MIX = "METS MIX";
    public static final String INJECTED_TYPE_METS = "METS REDUCED";
    
    private Map<String, byte[]> techMDMap = new HashMap<>();
    
    private static final ReentrantReadWriteLock metsLock = new ReentrantReadWriteLock();
    
    public MetsSplitter(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    @Override
    /**
     * This is tricky
     *
     * This method is invoked for ALL files in the batch and all injected events
     *
     * If the event is injected, we skip it
     * If the event is anything but a METS or TIFF file, we skip it.
     *
     * If the Event is a METS file:
     * The cause us to inject an event with the Reduced Mets file, i.e. the Mets file without the MIX metadata amdsev
     * We also store the Mix information, so we can use it later
     *
     * If the event is a TIFF File
     * We look up the tiff file in the amdSec structure to find the relevant MIX entry
     * inject this MIX as an event
     *
     * This WORKS in multithreaded IFF the "consumers" of these injected events runs in the same EventRunner as the Injector
     * This will normally be the case. There is one eventRunner for the batch, which forks off on each day, and joins afterwards
     * Because the METS file exists BEFORE any day, it is parsed by the overall eventRunner. This injects the ReducedMETS file,
     * which is checked by the MetsChecker, also running in this general EventRunner
     * Fiff file will be found inside each day, each in their own event runner. But because the injection of the MIX event
     * happens when we reach the tiff file, it will happen inside the day-EventRunner, and will ONLY be picked up by other checkers
     * for this specific day.
     *
     * Just trust that this works for MultiThreadedEventRunner
     *
     * @see MetsChecker
     * @see XpathPageChecker
     * @see dk.kb.kula190.iterators.eventhandlers.EventRunner
     * @see dk.kb.kula190.iterators.eventhandlers.MultiThreadedEventRunner
     *
     */
    public void handleAttribute(AttributeParsingEvent event) throws IOException {
        
        if (event instanceof InjectedAttributeParsingEvent) {
            // Do not handle our own injected events
            return;
        }
        String extension = EventHandlerUtils.getExtension(event.getName());
        //As M is before T, we will always have one thread parsing the Mets file before any threads start on the Tiff
        // files
        //But we need locks to ensure that the "Mets" thread complete before the "Tiff" threads start
        if (extension.equals("mets")) {
            log.info("Found mets file");
            metsFile(event);
            log.info("Done mets file");
        } else if (extension.equals("tif")) {
            tiffFile(event);
        }
    }
    
    private XPathSelector getXpath() {
        XPathSelector xpath = XpathUtils.createXPathSelector("mets", "http://www.loc.gov/METS/",
                                                             "dc", "http://purl.org/dc/elements/1.1/",
                                                             "mix", "http://purl.org/dc/elements/1.1/mix",
                                                             "premis", "http://purl.org/dc/elements/1.1/premis",
                                                             "xlink", "http://www.w3.org/1999/xlink");
        return xpath;
    }
    
    private void tiffFile(AttributeParsingEvent event) throws IOException {
        //Ensure we do not start to process this tiff file BEFORE the mets file have been processed
        //Read lock because we want no limit on the number of concurrent tiff file processings.
        ReentrantReadWriteLock.ReadLock metsReadLock = metsLock.readLock();
        metsReadLock.lock();
        try {
            
            String filename = EventHandlerUtils.lastName(event.getName());
    
    
            byte[] techMD = techMDMap.get(filename);
            
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
                pushEvent(event, INJECTED_TYPE_MIX, techMD);
           
        } finally {
            metsReadLock.unlock();
        }
    }
    
    private void metsFile(AttributeParsingEvent event) throws IOException {
        
        //Take a write lock to prevent any threads using amdSec or fileSec before we have completed them here
        ReentrantReadWriteLock.WriteLock metsWriteLock = metsLock.writeLock();
        metsWriteLock.lock();
        XPathSelector xpath = getXpath();
        try (InputStream data = event.getData()) {
            Document metsDoc = XML.fromXML(data, true);
    
            Node amdSec = xpath.selectNode(metsDoc, "/mets:mets")
                               .removeChild(xpath.selectNode(metsDoc, "/mets:mets/mets:amdSec"));
            Node fileSec = xpath.selectNode(metsDoc, "/mets:mets/mets:fileSec");
    
            List<Node> tiffFileNodes = xpath.selectNodeList(fileSec, "mets:fileGrp[@ID='TIFF']/mets:file");
            for (Node tiffFileNode : tiffFileNodes) {
                String id = tiffFileNode.getAttributes().getNamedItem("ID").getNodeValue();
                String admid = tiffFileNode.getAttributes().getNamedItem("ADMID").getNodeValue();
                String mimetype = tiffFileNode.getAttributes().getNamedItem("MIMETYPE").getNodeValue();
    
                String tiffRef = xpath.selectString(tiffFileNode, "mets:file/mets:FLocat/@xlink:href");
    
                Node techMD = xpath.selectNode(amdSec, "mets:techMD[@ID='" + admid + "']/mets:mdWrap/mets:xmlData/*");
                techMDMap.put(EventHandlerUtils.lastName(tiffRef), XML.domToString(techMD).getBytes(StandardCharsets.UTF_8));
            }
            
            metsDoc.normalizeDocument();
            
            pushEvent(event, INJECTED_TYPE_METS,
                      XML.domToString(metsDoc).getBytes(StandardCharsets.UTF_8));
            
        } catch (ParserConfigurationException | SAXException | TransformerException e) {
            throw new IOException("Failed to parse METS data from " + event.getLocation(), e);
        } finally {
            metsWriteLock.unlock();
        }
    }
}
