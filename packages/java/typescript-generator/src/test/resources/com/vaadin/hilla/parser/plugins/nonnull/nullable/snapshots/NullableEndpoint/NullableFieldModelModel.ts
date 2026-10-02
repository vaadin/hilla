import { _getPropertyModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type NullableFieldModel from "./NullableFieldModel.js";
class NullableFieldModelModel<T extends NullableFieldModel = NullableFieldModel> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(NullableFieldModelModel);
    get id(): StringModel {
        return this[_getPropertyModel]("id", (parent, key) => new StringModel(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.Id" }], javaType: "java.lang.String" } }));
    }
    get version(): NumberModel {
        return this[_getPropertyModel]("version", (parent, key) => new NumberModel(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.Version" }], javaType: "java.lang.Long" } }));
    }
    get jakartaNonnull(): StringModel {
        return this[_getPropertyModel]("jakartaNonnull", (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get jspecifyNonnull(): StringModel {
        return this[_getPropertyModel]("jspecifyNonnull", (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get springNonnull(): StringModel {
        return this[_getPropertyModel]("springNonnull", (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
}
export default NullableFieldModelModel;
