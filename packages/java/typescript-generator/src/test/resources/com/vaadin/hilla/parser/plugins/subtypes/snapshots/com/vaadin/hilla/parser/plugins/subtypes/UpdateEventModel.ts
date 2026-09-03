import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import BaseEventModel_1 from "./BaseEventModel.js";
import type UpdateEvent_1 from "./UpdateEvent.js";
class UpdateEventModel<T extends UpdateEvent_1 = UpdateEvent_1> extends BaseEventModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(UpdateEventModel);
    get oldItem(): StringModel_1 {
        return this[_getPropertyModel_1]("oldItem", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get newItem(): StringModel_1 {
        return this[_getPropertyModel_1]("newItem", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default UpdateEventModel;
