package com.yieldlab.gdpr.vendor;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
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
                && Objects.equals(purposes, that.purposes) && Objects.equals(features, that.features) && Objects.equals(
                vendors, that.vendors) && Objects.equals(additionalProperties, that.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vendorListVersion, lastUpdated, purposes, features, vendors, additionalProperties);
    }
}
