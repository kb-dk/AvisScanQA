package dk.kb.avischk.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigInteger;
import java.util.Date;

@XmlRootElement
public class NewspaperEntity {

    private String origRelpath;
    private String formatType;
    private Date editionDate;
    private boolean singlePage;
    private int pageNumber;
    private String avisid;
    private String avistitle;
    private String shadowPath;
    private String sectionTitle;
    private String editionTitle;
    private Date deliveryDate;
    private BigInteger handle;
    private boolean fraktur;
    
    public NewspaperEntity() {}

    public String getOrigRelpath() {
        return origRelpath;
    }

    public void setOrigRelpath(String origRelpath) {
        this.origRelpath = origRelpath;
    }

    public String getFormatType() {
        return formatType;
    }

    public void setFormatType(String formatType) {
        this.formatType = formatType;
    }

    public Date getEditionDate() {
        return editionDate;
    }

    public void setEditionDate(Date editionDate) {
        this.editionDate = editionDate;
    }

    public boolean isSinglePage() {
        return singlePage;
    }

    public void setSinglePage(boolean singlePage) {
        this.singlePage = singlePage;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getAvisid() {
        return avisid;
    }

    public void setAvisid(String avisid) {
        this.avisid = avisid;
    }

    public String getAvisTitle() {
        return avistitle;
    }

    public void setAvisTitle(String avistitle) {
        this.avistitle = avistitle;
    }

    public String getShadowPath() {
        return shadowPath;
    }

    public void setShadowPath(String shadowPath) {
        this.shadowPath = shadowPath;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public String getEditionTitle() {
        return editionTitle;
    }

    public void setEditionTitle(String editionTitle) {
        this.editionTitle = editionTitle;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
    
    public BigInteger getHandle() {
        return handle;
    }
    
    public void setHandle(BigInteger handle) {
        this.handle = handle;
    }

    public boolean isFraktur() {
        return fraktur;
    }

    public void setFraktur(boolean fraktur) {
        this.fraktur = fraktur;
    }
    
}
