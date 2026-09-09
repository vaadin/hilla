import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type Collection from "./Collection.js";
class CollectionModel<T extends Collection = Collection> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(CollectionModel);
    get author(): StringModel {
        return this[_getPropertyModel]("author", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get collectionName(): StringModel {
        return this[_getPropertyModel]("collectionName", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get type(): StringModel {
        return this[_getPropertyModel]("type", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default CollectionModel;
