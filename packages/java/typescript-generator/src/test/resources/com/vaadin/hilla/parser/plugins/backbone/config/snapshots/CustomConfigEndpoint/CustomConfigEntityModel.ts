import { _getPropertyModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type CustomConfigEntity from "./CustomConfigEntity.js";
class CustomConfigEntityModel<T extends CustomConfigEntity = CustomConfigEntity> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(CustomConfigEntityModel);
    get bar(): NumberModel {
        return this[_getPropertyModel]("bar", (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "int" } }));
    }
    get foo(): StringModel {
        return this[_getPropertyModel]("foo", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default CustomConfigEntityModel;
