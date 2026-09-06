import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1 } from "@vaadin/hilla-lit-form";
import type BinaryPayload_1 from "./BinaryPayload.js";
import PayloadModel_1 from "./PayloadModel.js";
class BinaryPayloadModel<T extends BinaryPayload_1 = BinaryPayload_1> extends PayloadModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(BinaryPayloadModel);
    get size(): NumberModel_1 {
        return this[_getPropertyModel_1]("size", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
    }
}
export default BinaryPayloadModel;
