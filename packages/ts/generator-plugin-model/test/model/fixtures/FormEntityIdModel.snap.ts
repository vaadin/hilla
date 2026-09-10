import m, { NotNull, NumberModel } from "@vaadin/hilla-models";
import type FormEntityId from "./FormEntityId.js";
const FormEntityIdModel = m
  .object<FormEntityId>("FormEntityId")
  .property("Id", m.constrained(NumberModel, NotNull()))
  .build();
type FormEntityIdModel = typeof FormEntityIdModel;
export default FormEntityIdModel;
