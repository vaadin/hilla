import m, { DecimalMax, DecimalMin, Digits, Email, Future, Max, Min, Negative, NegativeOrZero, NotBlank, NotEmpty, NotNull, Null, NumberModel, ObjectModel, Past, Pattern, Positive, PositiveOrZero, Size, StringModel } from "@vaadin/hilla-models";
import FormEntityModel from "./FormEntityModel.js";
import type FormValidationConstraints from "./FormValidationConstraints.js";
const FormValidationConstraintsModel: ObjectModel<FormValidationConstraints> = m
  .object<FormValidationConstraints>("FormValidationConstraints")
  .property("list", m.constrained(m.array(m.optional(StringModel)), NotEmpty()))
  .property("email", m.constrained(StringModel, Email({ message: "foo" })))
  .property("isNull", m.constrained(StringModel, Null()))
  .property("notNull", m.constrained(StringModel, NotNull()))
  .property("notEmpty", m.constrained(StringModel, NotEmpty(), NotNull()))
  .property("notNullEntity", m.constrained(m.lazy(() => FormEntityModel), NotNull()))
  .property("notBlank", m.constrained(StringModel, NotBlank()))
  .property("assertTrue", StringModel)
  .property("assertFalse", StringModel)
  .property("min", m.constrained(NumberModel, Min({ value: 1, message: "foo" })))
  .property("max", m.constrained(NumberModel, Max(2)))
  .property("decimalMin", m.constrained(NumberModel, DecimalMin("0.01")))
  .property("decimalMax", m.constrained(NumberModel, DecimalMax({ value: "0.01", inclusive: false })))
  .property("negative", m.constrained(NumberModel, Negative()))
  .property("negativeOrZero", m.constrained(NumberModel, NegativeOrZero()))
  .property("positive", m.constrained(NumberModel, Positive()))
  .property("positiveOrZero", m.constrained(NumberModel, PositiveOrZero()))
  .property("size", m.constrained(StringModel, Size()))
  .property("size1", m.constrained(StringModel, Size({ min: 1 })))
  .property("digits", m.constrained(StringModel, Digits({ integer: 5, fraction: 2 })))
  .property("past", m.constrained(StringModel, Past()))
  .property("future", m.constrained(StringModel, Future()))
  .property("pattern", m.constrained(StringModel, Pattern({ regexp: "\\d+\\..+" })))
  .build();
type FormValidationConstraintsModel = typeof FormValidationConstraintsModel;
export default FormValidationConstraintsModel;
