/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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
