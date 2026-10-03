import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import DeclaredBaseModel_1 from "../DeclaredBaseModel.js";
import type First_1 from "./First.js";
class FirstModel<T extends First_1 = First_1> extends DeclaredBaseModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(FirstModel);
    get note(): StringModel_1 {
        return this[_getPropertyModel_1]("note", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default FirstModel;
