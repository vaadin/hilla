import { _getPropertyModel, BooleanModel, makeObjectEmptyValueCreator, StringModel } from "@vaadin/hilla-lit-form";
import BaseEventModel from "./BaseEventModel.js";
import type DeleteEvent from "./DeleteEvent.js";
class DeleteEventModel<T extends DeleteEvent = DeleteEvent> extends BaseEventModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(DeleteEventModel);
    get item(): StringModel {
        return this[_getPropertyModel]("item", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get force(): BooleanModel {
        return this[_getPropertyModel]("force", (parent, key) => new BooleanModel(parent, key, false, { meta: { javaType: "boolean" } }));
    }
}
export default DeleteEventModel;
