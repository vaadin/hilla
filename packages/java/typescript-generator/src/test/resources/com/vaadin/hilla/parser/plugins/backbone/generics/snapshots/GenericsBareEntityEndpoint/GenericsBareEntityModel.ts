import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type GenericsBareEntity from "./GenericsBareEntity.js";
class GenericsBareEntityModel<T extends GenericsBareEntity = GenericsBareEntity> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(GenericsBareEntityModel);
    get bareEntityProperty(): StringModel {
        return this[_getPropertyModel]("bareEntityProperty", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default GenericsBareEntityModel;
