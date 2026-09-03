import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type NullableFieldModel_1 from "./NullableFieldModel.js";
class NullableFieldModelModel<T extends NullableFieldModel_1 = NullableFieldModel_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(NullableFieldModelModel);
    get id(): StringModel_1 {
        return this[_getPropertyModel_1]("id", (parent, key) => new StringModel_1(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.Id" }], javaType: "java.lang.String" } }));
    }
    get version(): NumberModel_1 {
        return this[_getPropertyModel_1]("version", (parent, key) => new NumberModel_1(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.Version" }], javaType: "java.lang.Long" } }));
    }
    get jakartaNonnull(): StringModel_1 {
        return this[_getPropertyModel_1]("jakartaNonnull", (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get jspecifyNonnull(): StringModel_1 {
        return this[_getPropertyModel_1]("jspecifyNonnull", (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get springNonnull(): StringModel_1 {
        return this[_getPropertyModel_1]("springNonnull", (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
}
export default NullableFieldModelModel;
