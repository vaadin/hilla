import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type Child_1 from "./Child.js";
import EmptyModel_1 from "./EmptyModel.js";
class ChildModel<T extends Child_1 = Child_1> extends EmptyModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(ChildModel);
    get name(): StringModel_1 {
        return this[_getPropertyModel_1]("name", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default ChildModel;
