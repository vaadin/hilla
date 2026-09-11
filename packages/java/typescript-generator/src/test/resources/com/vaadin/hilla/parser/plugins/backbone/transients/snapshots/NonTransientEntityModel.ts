import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type NonTransientEntity from "./NonTransientEntity.js";
class NonTransientEntityModel<T extends NonTransientEntity = NonTransientEntity> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(NonTransientEntityModel);
    get entityField(): StringModel {
        return this[_getPropertyModel]("entityField", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get transientWithGetter(): StringModel {
        return this[_getPropertyModel]("transientWithGetter", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default NonTransientEntityModel;
