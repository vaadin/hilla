import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type ComplexTypeModel_1 from "./ComplexTypeModel.js";
class ComplexTypeModelModel<T extends ComplexTypeModel_1 = ComplexTypeModel_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(ComplexTypeModelModel);
    get complexList(): ArrayModel_1<ObjectModel_1<Record<string, ReadonlyArray<string | undefined> | undefined>>> {
        return this[_getPropertyModel_1]("complexList", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new ObjectModel_1(parent, key, true, { meta: { javaType: "java.util.Map" } }), { meta: { javaType: "java.util.List" } }));
    }
    get complexMap(): ObjectModel_1<Record<string, ReadonlyArray<string | undefined> | undefined>> {
        return this[_getPropertyModel_1]("complexMap", (parent, key) => new ObjectModel_1(parent, key, true, { meta: { javaType: "java.util.Map" } }));
    }
}
export default ComplexTypeModelModel;
