package miraj.biid.com.pani_200;

/**
 * Created by Miraj on 21/6/2017.
 */

public class Field {

    private String fieldId;
    private String fieldName;
    private String farmerName;
    private String farmerPhoneNumber;
    private String farmerAddress;
    private String cropName;
    private String lspId;
    private String fieldLocation;
    private String fieldSowingDate;
    private String fieldIrrigationDate;
    private String fieldLspPhoneNumber;
    private boolean irrigationDone;
    private boolean requestedReSchedule;
    private boolean irrigationOff;
    private String suggestion;

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getFarmerPhoneNumber() {
        return farmerPhoneNumber;
    }

    public void setFarmerPhoneNumber(String farmerPhoneNumber) {
        this.farmerPhoneNumber = farmerPhoneNumber;
    }

    public String getFarmerAddress() {
        return farmerAddress;
    }

    public void setFarmerAddress(String farmerAddress) {
        this.farmerAddress = farmerAddress;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getLspId() {
        return lspId;
    }

    public void setLspId(String lspId) {
        this.lspId = lspId;
    }

    public String getFieldLocation() {
        return fieldLocation;
    }

    public void setFieldLocation(String fieldLocation) {
        this.fieldLocation = fieldLocation;
    }

    public String getFieldSowingDate() {
        return fieldSowingDate;
    }

    public void setFieldSowingDate(String fieldSowingDate) {
        this.fieldSowingDate = fieldSowingDate;
    }

    public String getFieldIrrigationDate() {
        return fieldIrrigationDate;
    }

    public void setFieldIrrigationDate(String fieldIrrigationDate) {
        this.fieldIrrigationDate = fieldIrrigationDate;
    }

    public String getFieldLspPhoneNumber() {
        return fieldLspPhoneNumber;
    }

    public void setFieldLspPhoneNumber(String fieldLspPhoneNumber) {
        this.fieldLspPhoneNumber = fieldLspPhoneNumber;
    }

    public boolean isIrrigationDone() {
        return irrigationDone;
    }

    public void setIrrigationDone(boolean irrigationDone) {
        this.irrigationDone = irrigationDone;
    }

    public boolean isRequestedReSchedule() {
        return requestedReSchedule;
    }

    public void setRequestedReSchedule(boolean requestedReSchedule) {
        this.requestedReSchedule = requestedReSchedule;
    }

    public boolean isIrrigationOff() {
        return irrigationOff;
    }

    public void setIrrigationOff(boolean irrigationOff) {
        this.irrigationOff = irrigationOff;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
}
