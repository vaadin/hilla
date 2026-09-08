import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import SoleBaseModel_1 from "../SoleBaseModel.js";
import type Only_1 from "./Only.js";
class OnlyModel<T extends Only_1 = Only_1> extends SoleBaseModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(OnlyModel);
    get note(): StringModel_1 {
        return this[_getPropertyModel_1]("note", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default OnlyModel;
