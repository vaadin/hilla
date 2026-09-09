import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel } from "@vaadin/hilla-lit-form";
import type Generic from "./Generic.js";
class GenericModel<T extends Generic = Generic> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(GenericModel);
    get genericField(): ObjectModel {
        return this[_getPropertyModel]("genericField", (parent, key) => new ObjectModel(parent, key, false));
    }
}
export default GenericModel;
