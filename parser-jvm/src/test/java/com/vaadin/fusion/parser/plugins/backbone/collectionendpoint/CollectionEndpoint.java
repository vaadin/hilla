package com.vaadin.fusion.parser.plugins.backbone.collectionendpoint;

import java.util.ArrayList;

import com.vaadin.fusion.parser.testutils.Endpoint;

@Endpoint
public class CollectionEndpoint {

    /**
     * Get a collection by author name. The generator should not mix this type
     * with the Java's Collection type.
     *
     * @param name
     *            author name
     * @return a collection
     */
    public Collection getCollectionByAuthor(String name) {
        return new Collection();
    }

    /**
     * Get a list of user name.
     *
     * @return list of user name
     */
    public java.util.Collection<String> getListOfUserName() {
        return new ArrayList<>();
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
