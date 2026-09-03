import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, BooleanModel as BooleanModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import CustomEntityModel_1 from "./CustomEntityModel.js";
import type JavaTypeTestEntity_1 from "./JavaTypeTestEntity.js";
class JavaTypeTestEntityModel<T extends JavaTypeTestEntity_1 = JavaTypeTestEntity_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(JavaTypeTestEntityModel);
    get aBoolean(): BooleanModel_1 {
        return this[_getPropertyModel_1]("aBoolean", (parent, key) => new BooleanModel_1(parent, key, false, { meta: { javaType: "boolean" } }));
    }
    get aNullableBoolean(): BooleanModel_1 {
        return this[_getPropertyModel_1]("aNullableBoolean", (parent, key) => new BooleanModel_1(parent, key, true, { meta: { javaType: "java.lang.Boolean" } }));
    }
    get aByte(): NumberModel_1 {
        return this[_getPropertyModel_1]("aByte", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "byte" } }));
    }
    get aNullableByte(): NumberModel_1 {
        return this[_getPropertyModel_1]("aNullableByte", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Byte" } }));
    }
    get aChar(): StringModel_1 {
        return this[_getPropertyModel_1]("aChar", (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "char" } }));
    }
    get aNullableChar(): StringModel_1 {
        return this[_getPropertyModel_1]("aNullableChar", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.Character" } }));
    }
    get aDouble(): NumberModel_1 {
        return this[_getPropertyModel_1]("aDouble", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "double" } }));
    }
    get aNullableDouble(): NumberModel_1 {
        return this[_getPropertyModel_1]("aNullableDouble", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Double" } }));
    }
    get aFloat(): NumberModel_1 {
        return this[_getPropertyModel_1]("aFloat", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "float" } }));
    }
    get aNullableFloat(): NumberModel_1 {
        return this[_getPropertyModel_1]("aNullableFloat", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Float" } }));
    }
    get aInt(): NumberModel_1 {
        return this[_getPropertyModel_1]("aInt", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
    }
    get aNullableInt(): NumberModel_1 {
        return this[_getPropertyModel_1]("aNullableInt", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Integer" } }));
    }
    get aLong(): NumberModel_1 {
        return this[_getPropertyModel_1]("aLong", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "long" } }));
    }
    get aNullableLong(): NumberModel_1 {
        return this[_getPropertyModel_1]("aNullableLong", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Long" } }));
    }
    get aShort(): NumberModel_1 {
        return this[_getPropertyModel_1]("aShort", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "short" } }));
    }
    get aNullableShort(): NumberModel_1 {
        return this[_getPropertyModel_1]("aNullableShort", (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Short" } }));
    }
    get aString(): StringModel_1 {
        return this[_getPropertyModel_1]("aString", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get aDate(): StringModel_1 {
        return this[_getPropertyModel_1]("aDate", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.util.Date" } }));
    }
    get aLocalDate(): StringModel_1 {
        return this[_getPropertyModel_1]("aLocalDate", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.time.LocalDate" } }));
    }
    get aLocalTime(): StringModel_1 {
        return this[_getPropertyModel_1]("aLocalTime", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.time.LocalTime" } }));
    }
    get aLocalDateTime(): StringModel_1 {
        return this[_getPropertyModel_1]("aLocalDateTime", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.time.LocalDateTime" } }));
    }
    get aStringArray(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("aStringArray", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.lang.String[]" } }));
    }
    get aByteArray(): ArrayModel_1<NumberModel_1> {
        return this[_getPropertyModel_1]("aByteArray", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "byte" } }), { meta: { javaType: "byte[]" } }));
    }
    get aStringList(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("aStringList", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.util.List" } }));
    }
    get aCustomEntity(): CustomEntityModel_1 {
        return this[_getPropertyModel_1]("aCustomEntity", (parent, key) => new CustomEntityModel_1(parent, key, true));
    }
}
export default JavaTypeTestEntityModel;
