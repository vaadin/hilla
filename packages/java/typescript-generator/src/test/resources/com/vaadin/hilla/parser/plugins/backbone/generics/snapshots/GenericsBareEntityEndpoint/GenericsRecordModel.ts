import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel } from "@vaadin/hilla-lit-form";
import type GenericsRecord from "./GenericsRecord.js";
class GenericsRecordModel<T extends GenericsRecord = GenericsRecord> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(GenericsRecordModel);
    get first(): ObjectModel {
        return this[_getPropertyModel]("first", (parent, key) => new ObjectModel(parent, key, true));
    }
    get second(): ObjectModel {
        return this[_getPropertyModel]("second", (parent, key) => new ObjectModel(parent, key, true));
    }
}
export default GenericsRecordModel;
