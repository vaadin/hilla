import { _getPropertyModel, ArrayModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type AnnotationTestEntity from "./AnnotationTestEntity.js";
import NestedEntityModel from "./NestedEntityModel.js";
class AnnotationTestEntityModel<T extends AnnotationTestEntity = AnnotationTestEntity> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(AnnotationTestEntityModel);
    get id(): NumberModel {
        return this[_getPropertyModel]("id", (parent, key) => new NumberModel(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.Id" }], javaType: "java.lang.Long" } }));
    }
    get version(): NumberModel {
        return this[_getPropertyModel]("version", (parent, key) => new NumberModel(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.Version" }], javaType: "int" } }));
    }
    get oneToOne(): NestedEntityModel {
        return this[_getPropertyModel]("oneToOne", (parent, key) => new NestedEntityModel(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.OneToOne" }] } }));
    }
    get oneToMany(): ArrayModel<NestedEntityModel> {
        return this[_getPropertyModel]("oneToMany", (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new NestedEntityModel(parent, key, true), { meta: { annotations: [{ name: "jakarta.persistence.OneToMany" }], javaType: "java.util.List" } }));
    }
    get manyToOne(): NestedEntityModel {
        return this[_getPropertyModel]("manyToOne", (parent, key) => new NestedEntityModel(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.ManyToOne" }] } }));
    }
    get manyToMany(): ArrayModel<NestedEntityModel> {
        return this[_getPropertyModel]("manyToMany", (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new NestedEntityModel(parent, key, true), { meta: { annotations: [{ name: "jakarta.persistence.ManyToMany" }], javaType: "java.util.List" } }));
    }
    get manyToManyWithFetchType(): ArrayModel<NestedEntityModel> {
        return this[_getPropertyModel]("manyToManyWithFetchType", (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new NestedEntityModel(parent, key, true), { meta: { annotations: [{ name: "jakarta.persistence.ManyToMany" }], javaType: "java.util.List" } }));
    }
    get name(): StringModel {
        return this[_getPropertyModel]("name", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default AnnotationTestEntityModel;
