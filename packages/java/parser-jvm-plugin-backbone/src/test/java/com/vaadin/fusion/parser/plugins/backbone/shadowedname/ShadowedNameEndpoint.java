package com.vaadin.fusion.parser.plugins.backbone.shadowedname;

import java.util.ArrayList;

@Endpoint
public class ShadowedNameEndpoint {
    /**
     * Get a list of user name.
     *
     * @return list of user name
     */
    public java.util.Collection<String> getJavaCollection() {
        return new ArrayList<>();
    }

    /**
     * Get a collection by author name. The generator should not mix this type
     * with the Java's Collection type.
     *
     * @param name
     *            author name
     * @return a collection
     */
    public Collection getNestedUserDefinedCollection(String name) {
        return new Collection();
    }

    public com.vaadin.fusion.parser.plugins.backbone.shadowedname.subpackage.Collection<String> getSeparateUserDefinedCollection() {
        return new com.vaadin.fusion.parser.plugins.backbone.shadowedname.subpackage.Collection<>();
    }

    public static class Collection {
        private String author;
        private String collectionName;
        private String type;

        public String getAuthor() {
            return author;
        }

        public String getCollectionName() {
            return collectionName;
        }

        public String getType() {
            return type;
        }
    }

}
