import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type MultiEndpointsSharedModel_1 from "./MultiEndpointsSharedModel.js";
class MultiEndpointsSharedModelModel<T extends MultiEndpointsSharedModel_1 = MultiEndpointsSharedModel_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(MultiEndpointsSharedModelModel);
    get id(): StringModel_1 {
        return this[_getPropertyModel_1]("id", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default MultiEndpointsSharedModelModel;
