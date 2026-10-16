import m, { StringModel } from "@vaadin/hilla-models";
import type T_1 from "./T.js";
const TModel = m
  .object<T_1>("T")
  .property("name", StringModel)
  .build();
type TModel = typeof TModel;
export default TModel;
