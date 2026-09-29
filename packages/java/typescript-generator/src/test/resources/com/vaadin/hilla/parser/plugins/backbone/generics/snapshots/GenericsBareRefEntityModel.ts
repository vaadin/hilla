import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel } from "@vaadin/hilla-lit-form";
import type GenericsBareRefEntity from "./GenericsBareRefEntity.js";
import GenericsBareRefEntityModel_1 from "./GenericsBareRefEntityModel.js";
class GenericsBareRefEntityModel<T extends GenericsBareRefEntity = GenericsBareRefEntity> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(GenericsBareRefEntityModel);
    get bareGenericTypeProperty(): ObjectModel {
        return this[_getPropertyModel]("bareGenericTypeProperty", (parent, key) => new ObjectModel(parent, key, true));
    }
    get bareRefEntityProperty(): GenericsBareRefEntityModel_1 {
        return this[_getPropertyModel]("bareRefEntityProperty", (parent, key) => new GenericsBareRefEntityModel_1(parent, key, true));
    }
}
export default GenericsBareRefEntityModel;
