import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@hilla/form";
import type AddEvent_1 from "./AddEvent.js";
import BaseEventModel_1 from "./BaseEventModel.js";
class AddEventModel<T extends AddEvent_1 = AddEvent_1> extends BaseEventModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(AddEventModel);
    get item(): StringModel_1 {
        return this[_getPropertyModel_1]("item", (parent, key) => new StringModel_1(parent, key, true));
    }
}
export default AddEventModel;
