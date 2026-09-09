import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type InheritedBase_1 from "./InheritedBase.js";
class InheritedBaseModel<T extends InheritedBase_1 = InheritedBase_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(InheritedBaseModel);
    get flavour(): StringModel_1 {
        return this[_getPropertyModel_1]("flavour", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get label(): StringModel_1 {
        return this[_getPropertyModel_1]("label", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default InheritedBaseModel;
