import m, { DecimalMax, DecimalMin, Digits, Email, Future, Max, Min, Negative, NegativeOrZero, NotBlank, NotEmpty, NotNull, Null, NumberModel, Past, Pattern, Positive, PositiveOrZero, Size, StringModel } from "@vaadin/hilla-models";
import type ValidationData from "./ValidationData.js";
const ValidationDataModel = m
  .object<ValidationData>("ValidationData")
  .property("assertFalse", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("assertTrue", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("decimalMax", m.meta(m.constrained(NumberModel, DecimalMax({ inclusive: false, value: "0.01" })), { jvmType: "double" }))
  .property("decimalMin", m.meta(m.constrained(NumberModel, DecimalMin("0.01")), { jvmType: "double" }))
  .property("digits", m.meta(m.constrained(m.optional(StringModel), Digits({ integer: 5, fraction: 2 })), { jvmType: "java.lang.String" }))
  .property("email", m.meta(m.constrained(m.optional(StringModel), Email({ message: "foo" })), { jvmType: "java.lang.String" }))
  .property("future", m.meta(m.constrained(m.optional(StringModel), Future()), { jvmType: "java.time.LocalDate" }))
  .property("isNull", m.meta(m.constrained(m.optional(StringModel), Null()), { jvmType: "java.lang.String" }))
  .property("list", m.meta(m.constrained(m.optional(m.array(m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))), NotEmpty()), { jvmType: "java.util.List" }))
  .property("max", m.meta(m.constrained(m.optional(NumberModel), Max(2)), { jvmType: "java.lang.Integer" }))
  .property("min", m.meta(m.constrained(m.optional(NumberModel), Min({ message: "foo", value: 1 })), { jvmType: "java.lang.Integer" }))
  .property("negative", m.meta(m.constrained(NumberModel, Negative()), { jvmType: "int" }))
  .property("negativeOrZero", m.meta(m.constrained(NumberModel, NegativeOrZero()), { jvmType: "int" }))
  .property("notBlank", m.meta(m.constrained(m.optional(StringModel), NotBlank()), { jvmType: "java.lang.String" }))
  .property("notEmpty", m.meta(m.constrained(m.optional(StringModel), NotNull(), NotEmpty()), { jvmType: "java.lang.String" }))
  .property("notNull", m.meta(m.constrained(m.optional(StringModel), NotNull()), { jvmType: "java.lang.String" }))
  .property("notNullEntity", m.constrained(m.optional(m.self), NotNull()))
  .property("past", m.meta(m.constrained(m.optional(StringModel), Past()), { jvmType: "java.time.LocalDate" }))
  .property("pattern", m.meta(m.constrained(m.optional(StringModel), Pattern({ regexp: "\\d+\\..+" })), { jvmType: "java.lang.String" }))
  .property("positive", m.meta(m.constrained(NumberModel, Positive()), { jvmType: "int" }))
  .property("positiveOrZero", m.meta(m.constrained(NumberModel, PositiveOrZero()), { jvmType: "int" }))
  .property("size", m.meta(m.constrained(m.optional(StringModel), Size()), { jvmType: "java.lang.String" }))
  .property("size1", m.meta(m.constrained(m.optional(StringModel), Size({ min: 1 })), { jvmType: "java.lang.String" }))
  .property("withConstraintsOnSetter", m.meta(m.constrained(m.optional(StringModel), NotNull(), NotBlank(), Email()), { jvmType: "java.lang.String" }))
  .property("withGetter", m.meta(m.constrained(m.optional(StringModel), NotBlank()), { jvmType: "java.lang.String" }))
  .property("withSetter", m.meta(m.constrained(m.optional(StringModel), Email(), NotBlank()), { jvmType: "java.lang.String" }))
  .build();
type ValidationDataModel = typeof ValidationDataModel;
export default ValidationDataModel;
