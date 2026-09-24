import { _getPropertyModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel } from "@vaadin/hilla-lit-form";
import type AbstractEntity from "./AbstractEntity.js";
class AbstractEntityModel<T extends AbstractEntity = AbstractEntity> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(AbstractEntityModel);
    get version(): NumberModel {
        return this[_getPropertyModel]("version", (parent, key) => new NumberModel(parent, key, false, { meta: { annotations: [{ name: "jakarta.persistence.Version" }], javaType: "int" } }));
    }
    get id(): NumberModel {
        return this[_getPropertyModel]("id", (parent, key) => new NumberModel(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.Id" }], javaType: "java.lang.Long" } }));
    }
}
export default AbstractEntityModel;
