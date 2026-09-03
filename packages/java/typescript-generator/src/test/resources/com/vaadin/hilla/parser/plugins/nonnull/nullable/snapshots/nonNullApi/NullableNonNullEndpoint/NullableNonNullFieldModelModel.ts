import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type NullableNonNullFieldModel_1 from "./NullableNonNullFieldModel.js";
class NullableNonNullFieldModelModel<T extends NullableNonNullFieldModel_1 = NullableNonNullFieldModel_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(NullableNonNullFieldModelModel);
    get required(): StringModel_1 {
        return this[_getPropertyModel_1]("required", (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get id(): StringModel_1 {
        return this[_getPropertyModel_1]("id", (parent, key) => new StringModel_1(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.Id" }], javaType: "java.lang.String" } }));
    }
    get version(): NumberModel_1 {
        return this[_getPropertyModel_1]("version", (parent, key) => new NumberModel_1(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.Version" }], javaType: "java.lang.Long" } }));
    }
    get notNullVersion(): NumberModel_1 {
        return this[_getPropertyModel_1]("notNullVersion", (parent, key) => new NumberModel_1(parent, key, false, { meta: { annotations: [{ name: "jakarta.persistence.Version" }], javaType: "java.lang.Long" } }));
    }
    get jakartaNullable(): StringModel_1 {
        return this[_getPropertyModel_1]("jakartaNullable", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get jspecifyNullable(): StringModel_1 {
        return this[_getPropertyModel_1]("jspecifyNullable", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get springNullable(): StringModel_1 {
        return this[_getPropertyModel_1]("springNullable", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default NullableNonNullFieldModelModel;
