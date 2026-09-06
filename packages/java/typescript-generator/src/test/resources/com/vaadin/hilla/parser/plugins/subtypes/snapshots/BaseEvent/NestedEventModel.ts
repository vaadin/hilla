import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import BaseEventModel_1 from "../BaseEventModel.js";
import type NestedEvent_1 from "./NestedEvent.js";
class NestedEventModel<T extends NestedEvent_1 = NestedEvent_1> extends BaseEventModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(NestedEventModel);
    get item(): StringModel_1 {
        return this[_getPropertyModel_1]("item", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default NestedEventModel;
