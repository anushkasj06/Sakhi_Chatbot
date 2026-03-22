package com.wms.dtos.response;

public class SupplierResponse {

    private final Long supplierId;
    private final String sName;
    private final String contactPerson;
    private final String email;

    public SupplierResponse(Long supplierId, String sName, String contactPerson, String email) {
        this.supplierId = supplierId;
        this.sName = sName;
        this.contactPerson = contactPerson;
        this.email = email;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public String getSName() {
        return sName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public String getEmail() {
        return email;
    }
}
