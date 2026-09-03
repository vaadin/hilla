import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type AnnotationTestEntity_1 from "./AnnotationTestEntity.js";
import NestedEntityModel_1 from "./NestedEntityModel.js";
class AnnotationTestEntityModel<T extends AnnotationTestEntity_1 = AnnotationTestEntity_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(AnnotationTestEntityModel);
    get id(): NumberModel_1 {
        return this[_getPropertyModel_1]("id", (parent, key) => new NumberModel_1(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.Id" }], javaType: "java.lang.Long" } }));
    }
    get version(): NumberModel_1 {
        return this[_getPropertyModel_1]("version", (parent, key) => new NumberModel_1(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.Version" }], javaType: "int" } }));
    }
    get oneToOne(): NestedEntityModel_1 {
        return this[_getPropertyModel_1]("oneToOne", (parent, key) => new NestedEntityModel_1(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.OneToOne" }] } }));
    }
    get oneToMany(): ArrayModel_1<NestedEntityModel_1> {
        return this[_getPropertyModel_1]("oneToMany", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new NestedEntityModel_1(parent, key, true), { meta: { annotations: [{ name: "jakarta.persistence.OneToMany" }], javaType: "java.util.List" } }));
    }
    get manyToOne(): NestedEntityModel_1 {
        return this[_getPropertyModel_1]("manyToOne", (parent, key) => new NestedEntityModel_1(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.ManyToOne" }] } }));
    }
    get manyToMany(): ArrayModel_1<NestedEntityModel_1> {
        return this[_getPropertyModel_1]("manyToMany", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new NestedEntityModel_1(parent, key, true), { meta: { annotations: [{ name: "jakarta.persistence.ManyToMany" }], javaType: "java.util.List" } }));
    }
    get manyToManyWithFetchType(): ArrayModel_1<NestedEntityModel_1> {
        return this[_getPropertyModel_1]("manyToManyWithFetchType", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new NestedEntityModel_1(parent, key, true), { meta: { annotations: [{ name: "jakarta.persistence.ManyToMany" }], javaType: "java.util.List" } }));
    }
    get name(): StringModel_1 {
        return this[_getPropertyModel_1]("name", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default AnnotationTestEntityModel;
