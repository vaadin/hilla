import { _getPropertyModel as _getPropertyModel_1, StringModel as StringModel_1 } from "@hilla/form";
import type AddEvent_1 from "./AddEvent.js";
import BaseEventModel_1 from "./BaseEventModel.js";
class AddEventModel<T extends AddEvent_1 = AddEvent_1> extends BaseEventModel_1<T> {
    declare static createEmptyValue: () => AddEvent_1;
    get item(): StringModel_1 {
        return this[_getPropertyModel_1]("item", StringModel_1, [true]) as StringModel_1;
    }
}
export default AddEventModel;
