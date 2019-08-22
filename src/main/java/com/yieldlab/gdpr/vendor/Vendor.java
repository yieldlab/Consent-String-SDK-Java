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
@JsonPropertyOrder({ "id", "name", "policyUrl", "deletedDate", "purposeIds", "legIntPurposeIds", "featureIds" })
public class Vendor {
    public static Vendor NONE = new Vendor();

    @JsonProperty("id")
    private int id = 0;
    @JsonProperty("name")
    private String name;
    @JsonProperty("policyUrl")
    private String policyUrl;
    @JsonProperty("deletedDate")
    private String deletedDate;
    @JsonProperty("purposeIds")
    private List<Integer> purposeIds = new ArrayList<>();
    @JsonProperty("legIntPurposeIds")
    private List<Integer> legIntPurposeIds = new ArrayList<>();
    @JsonProperty("featureIds")
    private List<Integer> featureIds = new ArrayList<>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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

    @JsonProperty("deletedDate")
    public String getDeletedDate() {
        return deletedDate;
    }

    @JsonProperty("deletedDate")
    public void setDeletedDate(String deletedDate) {
        this.deletedDate = deletedDate;
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
                && Objects.equals(deletedDate, vendor.deletedDate) && Objects.equals(purposeIds, vendor.purposeIds)
                && Objects.equals(legIntPurposeIds, vendor.legIntPurposeIds) && Objects.equals(featureIds,
                vendor.featureIds) && Objects.equals(additionalProperties, vendor.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, policyUrl, deletedDate, purposeIds, legIntPurposeIds, featureIds,
                additionalProperties);
    }
}
