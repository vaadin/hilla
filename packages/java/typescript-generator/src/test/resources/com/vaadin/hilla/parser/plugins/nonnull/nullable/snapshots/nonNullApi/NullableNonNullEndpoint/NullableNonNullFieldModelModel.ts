import { _getPropertyModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type NullableNonNullFieldModel from "./NullableNonNullFieldModel.js";
class NullableNonNullFieldModelModel<T extends NullableNonNullFieldModel = NullableNonNullFieldModel> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(NullableNonNullFieldModelModel);
    get required(): StringModel {
        return this[_getPropertyModel]("required", (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get id(): StringModel {
        return this[_getPropertyModel]("id", (parent, key) => new StringModel(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.Id" }], javaType: "java.lang.String" } }));
    }
    get version(): NumberModel {
        return this[_getPropertyModel]("version", (parent, key) => new NumberModel(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.Version" }], javaType: "java.lang.Long" } }));
    }
    get notNullVersion(): NumberModel {
        return this[_getPropertyModel]("notNullVersion", (parent, key) => new NumberModel(parent, key, false, { meta: { annotations: [{ name: "jakarta.persistence.Version" }], javaType: "java.lang.Long" } }));
    }
    get jakartaNullable(): StringModel {
        return this[_getPropertyModel]("jakartaNullable", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get jspecifyNullable(): StringModel {
        return this[_getPropertyModel]("jspecifyNullable", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get springNullable(): StringModel {
        return this[_getPropertyModel]("springNullable", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default NullableNonNullFieldModelModel;
