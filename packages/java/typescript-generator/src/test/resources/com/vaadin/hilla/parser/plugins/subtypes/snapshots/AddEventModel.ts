import { _getPropertyModel, makeObjectEmptyValueCreator, StringModel } from "@vaadin/hilla-lit-form";
import type AddEvent from "./AddEvent.js";
import BaseEventModel from "./BaseEventModel.js";
class AddEventModel<T extends AddEvent = AddEvent> extends BaseEventModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(AddEventModel);
    get item(): StringModel {
        return this[_getPropertyModel]("item", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default AddEventModel;
