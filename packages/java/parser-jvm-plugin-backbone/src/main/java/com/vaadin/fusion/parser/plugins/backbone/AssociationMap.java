package com.vaadin.fusion.parser.plugins.backbone;

import com.vaadin.fusion.parser.core.RelativeMethodInfo;
import com.vaadin.fusion.parser.core.RelativeTypeSignature;
import com.vaadin.fusion.parser.utils.BidirectionalMap;

import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;

public class AssociationMap extends BidirectionalMap<Object, Object> {
    public RelativeMethodInfo get(PathItem pathItem) {
        return (RelativeMethodInfo) super.get(pathItem);
    }

    public RelativeTypeSignature get(Schema<?> schema) {
        return (RelativeTypeSignature) super.get(schema);
    }

    public RelativeMethodInfo put(PathItem pathItem,
            RelativeMethodInfo method) {
        return (RelativeMethodInfo) super.put(pathItem, method);
    }

    public RelativeTypeSignature put(Schema<?> schema,
            RelativeTypeSignature type) {
        return (RelativeTypeSignature) super.put(schema, type);
    }

    @Override
    public Object put(Object key, Object value) {
        throw new UnsupportedOperationException(
                "AssociationMap does not support values with unknown type");
    }
}
