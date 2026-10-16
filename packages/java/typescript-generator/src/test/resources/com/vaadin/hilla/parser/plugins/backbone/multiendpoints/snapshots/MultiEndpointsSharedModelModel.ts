import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type MultiEndpointsSharedModel from "./MultiEndpointsSharedModel.js";
class MultiEndpointsSharedModelModel<T extends MultiEndpointsSharedModel = MultiEndpointsSharedModel> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(MultiEndpointsSharedModelModel);
    get id(): StringModel {
        return this[_getPropertyModel]("id", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default MultiEndpointsSharedModelModel;
