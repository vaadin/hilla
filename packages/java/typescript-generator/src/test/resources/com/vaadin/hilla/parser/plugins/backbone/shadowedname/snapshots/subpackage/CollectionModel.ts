import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type Collection_1 from "./Collection.js";
class CollectionModel<T extends Collection_1 = Collection_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(CollectionModel);
    get items(): ArrayModel_1<ObjectModel_1> {
        return this[_getPropertyModel_1]("items", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new ObjectModel_1(parent, key, true), { meta: { javaType: "java.util.List" } }));
    }
    get name(): StringModel_1 {
        return this[_getPropertyModel_1]("name", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default CollectionModel;
