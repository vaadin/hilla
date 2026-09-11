import { _getPropertyModel, ArrayModel, BooleanModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import CustomEntityModel from "./CustomEntityModel.js";
import type JavaTypeTestEntity from "./JavaTypeTestEntity.js";
class JavaTypeTestEntityModel<T extends JavaTypeTestEntity = JavaTypeTestEntity> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(JavaTypeTestEntityModel);
    get aBoolean(): BooleanModel {
        return this[_getPropertyModel]("aBoolean", (parent, key) => new BooleanModel(parent, key, false, { meta: { javaType: "boolean" } }));
    }
    get aNullableBoolean(): BooleanModel {
        return this[_getPropertyModel]("aNullableBoolean", (parent, key) => new BooleanModel(parent, key, true, { meta: { javaType: "java.lang.Boolean" } }));
    }
    get aByte(): NumberModel {
        return this[_getPropertyModel]("aByte", (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "byte" } }));
    }
    get aNullableByte(): NumberModel {
        return this[_getPropertyModel]("aNullableByte", (parent, key) => new NumberModel(parent, key, true, { meta: { javaType: "java.lang.Byte" } }));
    }
    get aChar(): StringModel {
        return this[_getPropertyModel]("aChar", (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "char" } }));
    }
    get aNullableChar(): StringModel {
        return this[_getPropertyModel]("aNullableChar", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.Character" } }));
    }
    get aDouble(): NumberModel {
        return this[_getPropertyModel]("aDouble", (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "double" } }));
    }
    get aNullableDouble(): NumberModel {
        return this[_getPropertyModel]("aNullableDouble", (parent, key) => new NumberModel(parent, key, true, { meta: { javaType: "java.lang.Double" } }));
    }
    get aFloat(): NumberModel {
        return this[_getPropertyModel]("aFloat", (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "float" } }));
    }
    get aNullableFloat(): NumberModel {
        return this[_getPropertyModel]("aNullableFloat", (parent, key) => new NumberModel(parent, key, true, { meta: { javaType: "java.lang.Float" } }));
    }
    get aInt(): NumberModel {
        return this[_getPropertyModel]("aInt", (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "int" } }));
    }
    get aNullableInt(): NumberModel {
        return this[_getPropertyModel]("aNullableInt", (parent, key) => new NumberModel(parent, key, true, { meta: { javaType: "java.lang.Integer" } }));
    }
    get aLong(): NumberModel {
        return this[_getPropertyModel]("aLong", (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "long" } }));
    }
    get aNullableLong(): NumberModel {
        return this[_getPropertyModel]("aNullableLong", (parent, key) => new NumberModel(parent, key, true, { meta: { javaType: "java.lang.Long" } }));
    }
    get aShort(): NumberModel {
        return this[_getPropertyModel]("aShort", (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "short" } }));
    }
    get aNullableShort(): NumberModel {
        return this[_getPropertyModel]("aNullableShort", (parent, key) => new NumberModel(parent, key, true, { meta: { javaType: "java.lang.Short" } }));
    }
    get aString(): StringModel {
        return this[_getPropertyModel]("aString", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get aDate(): StringModel {
        return this[_getPropertyModel]("aDate", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.util.Date" } }));
    }
    get aLocalDate(): StringModel {
        return this[_getPropertyModel]("aLocalDate", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.time.LocalDate" } }));
    }
    get aLocalTime(): StringModel {
        return this[_getPropertyModel]("aLocalTime", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.time.LocalTime" } }));
    }
    get aLocalDateTime(): StringModel {
        return this[_getPropertyModel]("aLocalDateTime", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.time.LocalDateTime" } }));
    }
    get aStringArray(): ArrayModel<StringModel> {
        return this[_getPropertyModel]("aStringArray", (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.lang.String[]" } }));
    }
    get aByteArray(): ArrayModel<NumberModel> {
        return this[_getPropertyModel]("aByteArray", (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "byte" } }), { meta: { javaType: "byte[]" } }));
    }
    get aStringList(): ArrayModel<StringModel> {
        return this[_getPropertyModel]("aStringList", (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.util.List" } }));
    }
    get aCustomEntity(): CustomEntityModel {
        return this[_getPropertyModel]("aCustomEntity", (parent, key) => new CustomEntityModel(parent, key, true));
    }
}
export default JavaTypeTestEntityModel;
