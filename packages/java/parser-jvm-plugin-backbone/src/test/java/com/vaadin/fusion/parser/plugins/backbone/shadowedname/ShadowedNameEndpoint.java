package com.vaadin.fusion.parser.plugins.backbone.shadowedname;

import java.util.ArrayList;

@Endpoint
public class ShadowedNameEndpoint {
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

    /**
     * Get a list of user name.
     *
     * @return list of user name
     */
    public java.util.Collection<String> getJavaCollection() {
        return new ArrayList<>();
    }

    public com.vaadin.fusion.parser.plugins.backbone.shadowedname.subpackage.Collection<String> getSeparateUserDefinedCollection() {
        return new com.vaadin.fusion.parser.plugins.backbone.shadowedname.subpackage.Collection<>();
    }

    public static class Collection {
        private String collectionName;
        private String type;
        private String author;

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
