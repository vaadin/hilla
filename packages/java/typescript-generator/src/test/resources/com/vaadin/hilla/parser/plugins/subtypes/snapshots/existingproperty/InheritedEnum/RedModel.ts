import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import InheritedEnumModel_1 from "../InheritedEnumModel.js";
import type Red_1 from "./Red.js";
class RedModel<T extends Red_1 = Red_1> extends InheritedEnumModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(RedModel);
    get note(): StringModel_1 {
        return this[_getPropertyModel_1]("note", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default RedModel;
