import { _getPropertyModel, ArrayModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type Collection from "./Collection.js";
class CollectionModel<T extends Collection = Collection> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(CollectionModel);
    get items(): ArrayModel<ObjectModel> {
        return this[_getPropertyModel]("items", (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new ObjectModel(parent, key, true), { meta: { javaType: "java.util.List" } }));
    }
    get name(): StringModel {
        return this[_getPropertyModel]("name", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default CollectionModel;
