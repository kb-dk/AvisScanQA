package dk.kb.kula190.checkers.batchcheckers.xpath;

import dk.kb.kula190.iterators.eventhandlers.decorating.DecoratedAttributeParsingEvent;
import dk.kb.util.xml.XPathSelector;
import dk.kb.util.xml.XpathUtils;
import org.w3c.dom.Node;

import java.time.LocalDate;

public class XpathMarc {
    private String marc130a;
    private String marc245a;
    private String marc250a;
    private String marc260a;
    private String marc260c;
    private String marc650a;
    private String marc650y;
    
    
    public void setMetsMarcInjectedFileData(DecoratedAttributeParsingEvent decoratedEvent,
                                            Node metadataMarc,
                                            String avis,
                                            String roundTrip,
                                            LocalDate startDate,
                                            LocalDate endDate) {
        
        XPathSelector xpath = XpathUtils.createXPathSelector(
                "marc", "http://www.loc.gov/MARC21/slim");
    
    
        marc130a = xpath.selectString(metadataMarc,
                                      "/marc:record/marc:datafield[@tag='130']/marc:subfield[@code='a']");
        marc245a = xpath.selectString(metadataMarc,
                                      "/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='a']");
    
        marc250a = xpath.selectString(metadataMarc,
                                      "/marc:record/marc:datafield[@tag='250']/marc:subfield[@code='a']");
    
        marc260a = xpath.selectString(metadataMarc,
                                      "/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='a']");
    
        marc260c = xpath.selectString(metadataMarc,
                                      "/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='c']");
    
        marc650a = xpath.selectString(metadataMarc,
                           "/marc:record/marc:datafield[@tag='650']/marc:subfield[@code='a']");
    
        marc650y = xpath.selectString(metadataMarc,
                                      "/marc:record/marc:datafield[@tag='650']/marc:subfield[@code='y']");
        //TODO whatever should be validated here???
    }
    
    /*
               <marc:record xmlns:marc="http://www.loc.gov/MARC21/slim">
                    <marc:leader>nas 22 uu 4500</marc:leader>
                    <marc:controlfield tag="008">| |||||||||||||||||dan||</marc:controlfield>
                    <marc:datafield ind1="1" ind2="0" tag="245">
                        <marc:subfield code="a">Modersmaalet (Haderslev)</marc:subfield>
                    </marc:datafield>
                    <marc:datafield ind1="0" ind2=" " tag="130">
                        <marc:subfield code="a">Modersmaalet (Haderslev) (1901-1938)</marc:subfield>
                    </marc:datafield>
                    <marc:datafield ind1=" " ind2=" " tag="250">
                        <marc:subfield code="a">serial</marc:subfield>
                    </marc:datafield>
                    <marc:datafield ind1=" " ind2=" " tag="260">
                        <marc:subfield code="a">Haderslev</marc:subfield>
                        <marc:subfield code="c">1901-02-11</marc:subfield>
                    </marc:datafield>
                    <marc:datafield ind1=" " ind2=" " tag="650">
                        <marc:subfield code="a">1901-02-11</marc:subfield>
                        <marc:subfield code="y">1938-06-14</marc:subfield>
                    </marc:datafield>
                </marc:record>
     */
    
    public String getMarc130a() {
        return marc130a;
    }
    
    public String getMarc245a() {
        return marc245a;
    }
    
    public String getMarc250a() {
        return marc250a;
    }
    
    public String getMarc260a() {
        return marc260a;
    }
    
    public String getMarc260c() {
        return marc260c;
    }
    
    public String getMarc650a() {
        return marc650a;
    }
    
    public String getMarc650y() {
        return marc650y;
    }
}
