import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type Generic_1 from "./Generic.js";
class GenericModel<T extends Generic_1 = Generic_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(GenericModel);
    get genericField(): ObjectModel_1 {
        return this[_getPropertyModel_1]("genericField", (parent, key) => new ObjectModel_1(parent, key, false));
    }
}
export default GenericModel;
