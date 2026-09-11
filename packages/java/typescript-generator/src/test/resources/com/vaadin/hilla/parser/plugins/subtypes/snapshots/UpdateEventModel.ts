import { _getPropertyModel, makeObjectEmptyValueCreator, StringModel } from "@vaadin/hilla-lit-form";
import BaseEventModel from "./BaseEventModel.js";
import type UpdateEvent from "./UpdateEvent.js";
class UpdateEventModel<T extends UpdateEvent = UpdateEvent> extends BaseEventModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(UpdateEventModel);
    get oldItem(): StringModel {
        return this[_getPropertyModel]("oldItem", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get newItem(): StringModel {
        return this[_getPropertyModel]("newItem", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default UpdateEventModel;
