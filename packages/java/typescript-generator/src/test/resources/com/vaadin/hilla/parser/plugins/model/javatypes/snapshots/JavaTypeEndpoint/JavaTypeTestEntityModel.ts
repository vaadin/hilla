import m, { BooleanModel, NumberModel, StringModel } from "@vaadin/hilla-models";
import CustomEntityModel from "./CustomEntityModel.js";
import type JavaTypeTestEntity from "./JavaTypeTestEntity.js";
const JavaTypeTestEntityModel = m
  .object<JavaTypeTestEntity>("JavaTypeTestEntity")
  .property("aBoolean", m.meta(BooleanModel, { jvmType: "boolean" }))
  .property("aNullableBoolean", m.meta(m.optional(BooleanModel), { jvmType: "java.lang.Boolean" }))
  .property("aByte", m.meta(NumberModel, { jvmType: "byte" }))
  .property("aNullableByte", m.meta(m.optional(NumberModel), { jvmType: "java.lang.Byte" }))
  .property("aChar", m.meta(StringModel, { jvmType: "char" }))
  .property("aNullableChar", m.meta(m.optional(StringModel), { jvmType: "java.lang.Character" }))
  .property("aDouble", m.meta(NumberModel, { jvmType: "double" }))
  .property("aNullableDouble", m.meta(m.optional(NumberModel), { jvmType: "java.lang.Double" }))
  .property("aFloat", m.meta(NumberModel, { jvmType: "float" }))
  .property("aNullableFloat", m.meta(m.optional(NumberModel), { jvmType: "java.lang.Float" }))
  .property("aInt", m.meta(NumberModel, { jvmType: "int" }))
  .property("aNullableInt", m.meta(m.optional(NumberModel), { jvmType: "java.lang.Integer" }))
  .property("aLong", m.meta(NumberModel, { jvmType: "long" }))
  .property("aNullableLong", m.meta(m.optional(NumberModel), { jvmType: "java.lang.Long" }))
  .property("aShort", m.meta(NumberModel, { jvmType: "short" }))
  .property("aNullableShort", m.meta(m.optional(NumberModel), { jvmType: "java.lang.Short" }))
  .property("aString", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("aDate", m.meta(m.optional(StringModel), { jvmType: "java.util.Date" }))
  .property("aLocalDate", m.meta(m.optional(StringModel), { jvmType: "java.time.LocalDate" }))
  .property("aLocalTime", m.meta(m.optional(StringModel), { jvmType: "java.time.LocalTime" }))
  .property("aLocalDateTime", m.meta(m.optional(StringModel), { jvmType: "java.time.LocalDateTime" }))
  .property("aStringArray", m.meta(m.optional(m.array(m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))), { jvmType: "java.lang.String[]" }))
  .property("aByteArray", m.meta(m.optional(m.array(m.meta(NumberModel, { jvmType: "byte" }))), { jvmType: "byte[]" }))
  .property("aStringList", m.meta(m.optional(m.array(m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))), { jvmType: "java.util.List" }))
  .property("aCustomEntity", m.optional(CustomEntityModel))
  .build();
type JavaTypeTestEntityModel = typeof JavaTypeTestEntityModel;
export default JavaTypeTestEntityModel;
