package com.yieldlab.gdpr.vendor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "vendorListVersion", "lastUpdated", "purposes", "features", "vendors" })
public class VendorList {

    @JsonProperty("vendorListVersion")
    private int vendorListVersion = 0;
    @JsonProperty("lastUpdated")
    private String lastUpdated = "";
    @JsonProperty("purposes")
    private List<Purpose> purposes = new ArrayList<>();
    @JsonProperty("features")
    private List<Feature> features = new ArrayList<>();
    @JsonProperty("vendors")
    private List<Vendor> vendors = new ArrayList<>();

    @JsonProperty("vendorListVersion")
    public int getVendorListVersion() {
        return vendorListVersion;
    }

    @JsonProperty("vendorListVersion")
    public void setVendorListVersion(int vendorListVersion) {
        this.vendorListVersion = vendorListVersion;
    }

    @JsonProperty("lastUpdated")
    public String getLastUpdated() {
        return lastUpdated;
    }

    @JsonProperty("lastUpdated")
    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @JsonProperty("purposes")
    public List<Purpose> getPurposes() {
        return purposes;
    }

    @JsonProperty("purposes")
    public void setPurposes(List<Purpose> purposes) {
        this.purposes = purposes;
    }

    @JsonProperty("features")
    public List<Feature> getFeatures() {
        return features;
    }

    @JsonProperty("features")
    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    @JsonProperty("vendors")
    public List<Vendor> getVendors() {
        return vendors;
    }

    @JsonProperty("vendors")
    public void setVendors(List<Vendor> vendors) {
        this.vendors = vendors;
    }

    @Override
    public String toString() {
        return "VendorList{" + "vendorListVersion=" + vendorListVersion + ", lastUpdated='" + lastUpdated + '\''
                + ", purposes=" + purposes + ", features=" + features + ", vendors=" + vendors + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        VendorList that = (VendorList) o;
        return vendorListVersion == that.vendorListVersion && Objects.equals(lastUpdated, that.lastUpdated)
                && Objects.equals(purposes, that.purposes) && Objects.equals(features, that.features)
                && Objects.equals(vendors, that.vendors);
    }

    @Override
    public int hashCode() {

        return Objects.hash(vendorListVersion, lastUpdated, purposes, features, vendors);
    }
}
