import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import InheritedBaseModel_1 from "../InheritedBaseModel.js";
import type Sweet_1 from "./Sweet.js";
class SweetModel<T extends Sweet_1 = Sweet_1> extends InheritedBaseModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(SweetModel);
    get note(): StringModel_1 {
        return this[_getPropertyModel_1]("note", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default SweetModel;
