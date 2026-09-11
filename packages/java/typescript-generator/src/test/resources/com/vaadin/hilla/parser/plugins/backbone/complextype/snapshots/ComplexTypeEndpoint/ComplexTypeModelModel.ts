import { _getPropertyModel, ArrayModel, makeObjectEmptyValueCreator, ObjectModel } from "@vaadin/hilla-lit-form";
import type ComplexTypeModel from "./ComplexTypeModel.js";
class ComplexTypeModelModel<T extends ComplexTypeModel = ComplexTypeModel> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(ComplexTypeModelModel);
    get complexList(): ArrayModel<ObjectModel<Record<string, ReadonlyArray<string | undefined> | undefined>>> {
        return this[_getPropertyModel]("complexList", (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new ObjectModel(parent, key, true, { meta: { javaType: "java.util.Map" } }), { meta: { javaType: "java.util.List" } }));
    }
    get complexMap(): ObjectModel<Record<string, ReadonlyArray<string | undefined> | undefined>> {
        return this[_getPropertyModel]("complexMap", (parent, key) => new ObjectModel(parent, key, true, { meta: { javaType: "java.util.Map" } }));
    }
}
export default ComplexTypeModelModel;
