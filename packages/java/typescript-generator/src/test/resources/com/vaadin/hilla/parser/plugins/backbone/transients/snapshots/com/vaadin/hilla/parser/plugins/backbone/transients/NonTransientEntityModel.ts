import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type NonTransientEntity_1 from "./NonTransientEntity.js";
class NonTransientEntityModel<T extends NonTransientEntity_1 = NonTransientEntity_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(NonTransientEntityModel);
    get entityField(): StringModel_1 {
        return this[_getPropertyModel_1]("entityField", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get transientWithGetter(): StringModel_1 {
        return this[_getPropertyModel_1]("transientWithGetter", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default NonTransientEntityModel;
