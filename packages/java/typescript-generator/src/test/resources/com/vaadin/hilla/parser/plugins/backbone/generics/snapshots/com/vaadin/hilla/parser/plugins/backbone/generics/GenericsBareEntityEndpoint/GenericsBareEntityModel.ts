import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type GenericsBareEntity_1 from "./GenericsBareEntity.js";
class GenericsBareEntityModel<T extends GenericsBareEntity_1 = GenericsBareEntity_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(GenericsBareEntityModel);
    get bareEntityProperty(): StringModel_1 {
        return this[_getPropertyModel_1]("bareEntityProperty", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default GenericsBareEntityModel;
