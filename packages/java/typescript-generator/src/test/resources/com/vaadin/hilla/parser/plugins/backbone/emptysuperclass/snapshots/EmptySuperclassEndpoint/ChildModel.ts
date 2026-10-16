import { _getPropertyModel, makeObjectEmptyValueCreator, StringModel } from "@vaadin/hilla-lit-form";
import type Child from "./Child.js";
import EmptyModel from "./EmptyModel.js";
class ChildModel<T extends Child = Child> extends EmptyModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(ChildModel);
    get name(): StringModel {
        return this[_getPropertyModel]("name", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default ChildModel;
