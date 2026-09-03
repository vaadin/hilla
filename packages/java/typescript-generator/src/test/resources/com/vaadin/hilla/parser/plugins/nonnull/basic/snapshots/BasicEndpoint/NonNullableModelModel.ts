import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type NonNullableModel_1 from "./NonNullableModel.js";
class NonNullableModelModel<T extends NonNullableModel_1 = NonNullableModel_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(NonNullableModelModel);
    get complexTypeField(): ObjectModel_1<Record<string, ReadonlyArray<NonNullableModel_1>>> {
        return this[_getPropertyModel_1]("complexTypeField", (parent, key) => new ObjectModel_1(parent, key, false, { meta: { javaType: "java.util.Map" } }));
    }
    get nullableField(): StringModel_1 {
        return this[_getPropertyModel_1]("nullableField", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get protectedField(): StringModel_1 {
        return this[_getPropertyModel_1]("protectedField", (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get publicField(): StringModel_1 {
        return this[_getPropertyModel_1]("publicField", (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get typeWithTypeArgument(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("typeWithTypeArgument", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.util.List" } }));
    }
}
export default NonNullableModelModel;
