import { _getPropertyModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel } from "@vaadin/hilla-lit-form";
import type BaseEvent from "./BaseEvent.js";
class BaseEventModel<T extends BaseEvent = BaseEvent> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(BaseEventModel);
    get id(): NumberModel {
        return this[_getPropertyModel]("id", (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "int" } }));
    }
}
export default BaseEventModel;
