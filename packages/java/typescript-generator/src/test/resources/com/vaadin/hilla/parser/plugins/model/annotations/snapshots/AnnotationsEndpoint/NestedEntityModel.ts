import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type NestedEntity from "./NestedEntity.js";
class NestedEntityModel<T extends NestedEntity = NestedEntity> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(NestedEntityModel);
    get name(): StringModel {
        return this[_getPropertyModel]("name", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default NestedEntityModel;
