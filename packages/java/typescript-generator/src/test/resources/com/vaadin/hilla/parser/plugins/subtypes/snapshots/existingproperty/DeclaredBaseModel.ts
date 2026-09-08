import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type DeclaredBase_1 from "./DeclaredBase.js";
class DeclaredBaseModel<T extends DeclaredBase_1 = DeclaredBase_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(DeclaredBaseModel);
    get label(): StringModel_1 {
        return this[_getPropertyModel_1]("label", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default DeclaredBaseModel;
