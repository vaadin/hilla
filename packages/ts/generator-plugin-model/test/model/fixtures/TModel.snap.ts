import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type T_1 from "./T.js";
class TModel<T extends T_1 = T_1> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(TModel);
    get name(): StringModel {
        return this[_getPropertyModel]("name", (parent, key) => new StringModel(parent, key, false));
    }
}
export default TModel;
