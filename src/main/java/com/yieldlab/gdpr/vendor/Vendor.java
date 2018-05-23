package com.yieldlab.gdpr.vendor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "name", "policyUrl", "purposeIds", "legIntPurposeIds", "featureIds" })
public class Vendor {
    public static Vendor NONE = new Vendor();

    @JsonProperty("id")
    private int id = 0;
    @JsonProperty("name")
    private String name = "";
    @JsonProperty("policyUrl")
    private String policyUrl = "";
    @JsonProperty("purposeIds")
    private List<Integer> purposeIds = new ArrayList<>();
    @JsonProperty("legIntPurposeIds")
    private List<Integer> legIntPurposeIds = new ArrayList<>();
    @JsonProperty("featureIds")
    private List<Integer> featureIds = new ArrayList<>();

    @JsonProperty("id")
    public int getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(int id) {
        this.id = id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("policyUrl")
    public String getPolicyUrl() {
        return policyUrl;
    }

    @JsonProperty("policyUrl")
    public void setPolicyUrl(String policyUrl) {
        this.policyUrl = policyUrl;
    }

    @JsonProperty("purposeIds")
    public List<Integer> getPurposeIds() {
        return purposeIds;
    }

    @JsonProperty("purposeIds")
    public void setPurposeIds(List<Integer> purposeIds) {
        this.purposeIds = purposeIds;
    }

    @JsonProperty("legIntPurposeIds")
    public List<Integer> getLegIntPurposeIds() {
        return legIntPurposeIds;
    }

    @JsonProperty("legIntPurposeIds")
    public void setLegIntPurposeIds(List<Integer> legIntPurposeIds) {
        this.legIntPurposeIds = legIntPurposeIds;
    }

    @JsonProperty("featureIds")
    public List<Integer> getFeatureIds() {
        return featureIds;
    }

    @JsonProperty("featureIds")
    public void setFeatureIds(List<Integer> featureIds) {
        this.featureIds = featureIds;
    }

    @Override
    public String toString() {
        return "Vendor{" + "id=" + id + ", name='" + name + '\'' + ", policyUrl='" + policyUrl + '\'' + ", purposeIds="
                + purposeIds + ", legIntPurposeIds=" + legIntPurposeIds + ", featureIds=" + featureIds + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Vendor vendor = (Vendor) o;
        return id == vendor.id && Objects.equals(name, vendor.name) && Objects.equals(policyUrl, vendor.policyUrl)
                && Objects.equals(purposeIds, vendor.purposeIds)
                && Objects.equals(legIntPurposeIds, vendor.legIntPurposeIds)
                && Objects.equals(featureIds, vendor.featureIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, policyUrl, purposeIds, legIntPurposeIds, featureIds);
    }
}
