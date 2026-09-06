import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import BaseEventModel_1 from "./BaseEventModel.js";
import type MoveEvent_1 from "./MoveEvent.js";
class MoveEventModel<T extends MoveEvent_1 = MoveEvent_1> extends BaseEventModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(MoveEventModel);
    get item(): StringModel_1 {
        return this[_getPropertyModel_1]("item", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get position(): NumberModel_1 {
        return this[_getPropertyModel_1]("position", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
    }
}
export default MoveEventModel;
