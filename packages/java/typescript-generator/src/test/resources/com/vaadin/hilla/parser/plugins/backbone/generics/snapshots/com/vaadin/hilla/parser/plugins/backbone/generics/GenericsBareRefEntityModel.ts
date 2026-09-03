import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type GenericsBareRefEntity_1 from "./GenericsBareRefEntity.js";
import GenericsBareRefEntityModel_1 from "./GenericsBareRefEntityModel.js";
class GenericsBareRefEntityModel<T extends GenericsBareRefEntity_1 = GenericsBareRefEntity_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(GenericsBareRefEntityModel);
    get bareGenericTypeProperty(): ObjectModel_1 {
        return this[_getPropertyModel_1]("bareGenericTypeProperty", (parent, key) => new ObjectModel_1(parent, key, true));
    }
    get bareRefEntityProperty(): GenericsBareRefEntityModel_1 {
        return this[_getPropertyModel_1]("bareRefEntityProperty", (parent, key) => new GenericsBareRefEntityModel_1(parent, key, true));
    }
}
export default GenericsBareRefEntityModel;
