import { _getPropertyModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel } from "@vaadin/hilla-lit-form";
import type AbstractEntity from "./AbstractEntity.js";
class AbstractEntityModel<T extends AbstractEntity = AbstractEntity> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(AbstractEntityModel);
    get version(): NumberModel {
        return this[_getPropertyModel]("version", (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "int" } }));
    }
    get id(): ObjectModel {
        return this[_getPropertyModel]("id", (parent, key) => new ObjectModel(parent, key, false));
    }
}
export default AbstractEntityModel;
