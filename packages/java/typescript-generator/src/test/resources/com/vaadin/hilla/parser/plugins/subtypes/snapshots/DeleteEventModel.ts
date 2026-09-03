import { _getPropertyModel as _getPropertyModel_1, BooleanModel as BooleanModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import BaseEventModel_1 from "./BaseEventModel.js";
import type DeleteEvent_1 from "./DeleteEvent.js";
class DeleteEventModel<T extends DeleteEvent_1 = DeleteEvent_1> extends BaseEventModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(DeleteEventModel);
    get item(): StringModel_1 {
        return this[_getPropertyModel_1]("item", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get force(): BooleanModel_1 {
        return this[_getPropertyModel_1]("force", (parent, key) => new BooleanModel_1(parent, key, false, { meta: { javaType: "boolean" } }));
    }
}
export default DeleteEventModel;
