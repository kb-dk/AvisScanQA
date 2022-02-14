package dk.kb.avischk.dto;

import java.util.Date;

public class CharacterizationInfo {

    private String origRelpath;
    private String tool;
    private Date characterisationDate;
    private String toolOutput;
    private String status;
    
    public CharacterizationInfo() {}

    public String getOrigRelpath() {
        return origRelpath;
    }

    public void setOrigRelpath(String origRelpath) {
        this.origRelpath = origRelpath;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public Date getCharacterisationDate() {
        return characterisationDate;
    }

    public void setCharacterisationDate(Date characterisationDate) {
        this.characterisationDate = characterisationDate;
    }

    public String getToolOutput() {
        return toolOutput;
    }

    public void setToolOutput(String toolOutput) {
        this.toolOutput = toolOutput;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
