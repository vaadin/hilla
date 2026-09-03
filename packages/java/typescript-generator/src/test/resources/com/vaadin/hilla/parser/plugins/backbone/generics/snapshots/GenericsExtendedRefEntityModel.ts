import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import GenericsBareRefEntityModel_1 from "./GenericsBareRefEntityModel.js";
import type GenericsExtendedRefEntity_1 from "./GenericsExtendedRefEntity.js";
class GenericsExtendedRefEntityModel<T extends GenericsExtendedRefEntity_1 = GenericsExtendedRefEntity_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(GenericsExtendedRefEntityModel);
    get extendedGenericTypeReference(): GenericsBareRefEntityModel_1 {
        return this[_getPropertyModel_1]("extendedGenericTypeReference", (parent, key) => new GenericsBareRefEntityModel_1(parent, key, true));
    }
}
export default GenericsExtendedRefEntityModel;
