import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel } from "@vaadin/hilla-lit-form";
import GenericsBareRefEntityModel from "./GenericsBareRefEntityModel.js";
import type GenericsExtendedRefEntity from "./GenericsExtendedRefEntity.js";
class GenericsExtendedRefEntityModel<T extends GenericsExtendedRefEntity = GenericsExtendedRefEntity> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(GenericsExtendedRefEntityModel);
    get extendedGenericTypeReference(): GenericsBareRefEntityModel {
        return this[_getPropertyModel]("extendedGenericTypeReference", (parent, key) => new GenericsBareRefEntityModel(parent, key, true));
    }
}
export default GenericsExtendedRefEntityModel;
