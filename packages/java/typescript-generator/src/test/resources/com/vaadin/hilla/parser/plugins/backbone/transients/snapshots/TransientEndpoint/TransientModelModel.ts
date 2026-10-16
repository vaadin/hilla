import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import NonTransientEntityModel from "../NonTransientEntityModel.js";
import type TransientModel from "./TransientModel.js";
class TransientModelModel<T extends TransientModel = TransientModel> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(TransientModelModel);
    get nonTransientEntity(): NonTransientEntityModel {
        return this[_getPropertyModel]("nonTransientEntity", (parent, key) => new NonTransientEntityModel(parent, key, true));
    }
    get notTransientField(): StringModel {
        return this[_getPropertyModel]("notTransientField", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default TransientModelModel;
