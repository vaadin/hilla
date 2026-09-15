import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type CustomEntity from "./CustomEntity.js";
class CustomEntityModel<T extends CustomEntity = CustomEntity> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(CustomEntityModel);
    get value(): StringModel {
        return this[_getPropertyModel]("value", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default CustomEntityModel;
