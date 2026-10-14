package com.realestate.model;

public enum Role {
    ROLE_ADMIN,
    ROLE_PROPERTY_MANAGER,
    ROLE_TENANT;

    public String getDisplayName() {
        switch (this) {
            case ROLE_ADMIN:
                return "Administrator";
            case ROLE_PROPERTY_MANAGER:
                return "Property Manager";
            case ROLE_TENANT:
                return "Tenant";
            default:
                return this.name();
        }
    }
}
