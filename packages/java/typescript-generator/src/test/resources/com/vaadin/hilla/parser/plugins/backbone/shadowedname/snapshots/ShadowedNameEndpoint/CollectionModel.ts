import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type Collection_1 from "./Collection.js";
class CollectionModel<T extends Collection_1 = Collection_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(CollectionModel);
    get author(): StringModel_1 {
        return this[_getPropertyModel_1]("author", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get collectionName(): StringModel_1 {
        return this[_getPropertyModel_1]("collectionName", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get type(): StringModel_1 {
        return this[_getPropertyModel_1]("type", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default CollectionModel;
