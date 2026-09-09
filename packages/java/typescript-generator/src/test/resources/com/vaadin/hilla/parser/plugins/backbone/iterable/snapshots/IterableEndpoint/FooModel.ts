import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type Foo from "./Foo.js";
class FooModel<T extends Foo = Foo> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(FooModel);
    get bar(): StringModel {
        return this[_getPropertyModel]("bar", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default FooModel;
