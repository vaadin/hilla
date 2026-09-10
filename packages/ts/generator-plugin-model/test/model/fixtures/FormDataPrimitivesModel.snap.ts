import m, { BooleanModel, NumberModel, StringModel } from "@vaadin/hilla-models";
import type FormDataPrimitives from "./FormDataPrimitives.js";
const FormDataPrimitivesModel = m
  .object<FormDataPrimitives>("FormDataPrimitives")
  .property("stringProp", StringModel)
  .property("longWrapperProp", NumberModel)
  .property("longProp", NumberModel)
  .property("integerWrapperProp", NumberModel)
  .property("integerProp", NumberModel)
  .property("doubleWrapperProp", NumberModel)
  .property("doubleProp", NumberModel)
  .property("floatWrapperProp", NumberModel)
  .property("floatProp", NumberModel)
  .property("booleanWrapperProp", BooleanModel)
  .property("booleanProp", BooleanModel)
  .build();
type FormDataPrimitivesModel = typeof FormDataPrimitivesModel;
export default FormDataPrimitivesModel;
