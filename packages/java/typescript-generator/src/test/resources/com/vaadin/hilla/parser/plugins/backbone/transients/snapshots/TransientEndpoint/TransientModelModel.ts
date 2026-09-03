import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import NonTransientEntityModel_1 from "../NonTransientEntityModel.js";
import type TransientModel_1 from "./TransientModel.js";
class TransientModelModel<T extends TransientModel_1 = TransientModel_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(TransientModelModel);
    get nonTransientEntity(): NonTransientEntityModel_1 {
        return this[_getPropertyModel_1]("nonTransientEntity", (parent, key) => new NonTransientEntityModel_1(parent, key, true));
    }
    get notTransientField(): StringModel_1 {
        return this[_getPropertyModel_1]("notTransientField", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default TransientModelModel;
