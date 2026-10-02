import { _getPropertyModel, ArrayModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type Entity from "./Entity.js";
class EntityModel<T extends Entity = Entity> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(EntityModel);
    get nonnullListOfNullableStrings(): ArrayModel<StringModel> {
        return this[_getPropertyModel]("nonnullListOfNullableStrings", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.util.List" } }));
    }
}
export default EntityModel;
