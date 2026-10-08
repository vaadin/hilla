import { _getPropertyModel, ArrayModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type NonNullableModel from "./NonNullableModel.js";
class NonNullableModelModel<T extends NonNullableModel = NonNullableModel> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(NonNullableModelModel);
    get complexTypeField(): ObjectModel<Record<string, ReadonlyArray<NonNullableModel>>> {
        return this[_getPropertyModel]("complexTypeField", (parent, key) => new ObjectModel(parent, key, false, { meta: { javaType: "java.util.Map" } }));
    }
    get nullableField(): StringModel {
        return this[_getPropertyModel]("nullableField", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get protectedField(): StringModel {
        return this[_getPropertyModel]("protectedField", (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get publicField(): StringModel {
        return this[_getPropertyModel]("publicField", (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get typeWithTypeArgument(): ArrayModel<StringModel> {
        return this[_getPropertyModel]("typeWithTypeArgument", (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.util.List" } }));
    }
}
export default NonNullableModelModel;
