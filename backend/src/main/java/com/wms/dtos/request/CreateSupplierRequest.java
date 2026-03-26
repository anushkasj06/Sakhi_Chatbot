package com.wms.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateSupplierRequest {

    @NotBlank
    @Size(max = 255)
    @JsonProperty("sName")
    private String sName;

    @NotBlank
    @Size(max = 255)
    private String contactPerson;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    public String getSName() {
        return sName;
    }

    public void setSName(String sName) {
        this.sName = sName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
