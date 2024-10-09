package com.vaadin.flow.connect;

public class ObjectWithDifferentAccessMethods {
    private String privateProp;
    private String protectedProp;
    private String publicProp;
    private String packagePrivateProp;
    private String publicGetterProp;
    private String publicSetterProp;

    public ObjectWithDifferentAccessMethods(String privateProp,
            String protectedProp, String publicProp, String packagePrivateProp,
            String publicGetterProp, String publicSetterProp) {
        this.privateProp = privateProp;
        this.protectedProp = protectedProp;
        this.publicProp = publicProp;
        this.packagePrivateProp = packagePrivateProp;
        this.publicGetterProp = publicGetterProp;
        this.publicSetterProp = publicSetterProp;
    }

    private String getPrivateProp() {
        return privateProp;
    }

    private void setPrivateProp(String privateProp) {
        this.privateProp = privateProp;
    }

    protected String getProtectedProp() {
        return protectedProp;
    }

    protected void setProtectedProp(String protectedProp) {
        this.protectedProp = protectedProp;
    }

    public String getPublicProp() {
        return publicProp;
    }

    public void setPublicProp(String publicProp) {
        this.publicProp = publicProp;
    }

    String getPackagePrivateProp() {
        return packagePrivateProp;
    }

    void setPackagePrivateProp(String packagePrivateProp) {
        this.packagePrivateProp = packagePrivateProp;
    }

    public String getPublicGetterProp() {
        return publicGetterProp;
    }

    private void setPublicGetterProp(String publicGetterProp) {
        this.publicGetterProp = publicGetterProp;
    }

    private String getPublicSetterProp() {
        return publicSetterProp;
    }

    public void setPublicSetterProp(String publicSetterProp) {
        this.publicSetterProp = publicSetterProp;
    }
}
