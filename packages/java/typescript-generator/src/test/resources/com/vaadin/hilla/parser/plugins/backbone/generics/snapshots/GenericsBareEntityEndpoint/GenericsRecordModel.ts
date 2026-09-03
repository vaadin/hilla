import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type GenericsRecord_1 from "./GenericsRecord.js";
class GenericsRecordModel<T extends GenericsRecord_1 = GenericsRecord_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(GenericsRecordModel);
    get first(): ObjectModel_1 {
        return this[_getPropertyModel_1]("first", (parent, key) => new ObjectModel_1(parent, key, true));
    }
    get second(): ObjectModel_1 {
        return this[_getPropertyModel_1]("second", (parent, key) => new ObjectModel_1(parent, key, true));
    }
}
export default GenericsRecordModel;
