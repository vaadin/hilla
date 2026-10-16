import m, { NumberModel } from "@vaadin/hilla-models";
import type Pageable from "./Pageable.js";
import SortModel from "./SortModel.js";
const PageableModel = m
  .object<Pageable>("Pageable")
  .property("pageNumber", m.meta(NumberModel, { jvmType: "int" }))
  .property("pageSize", m.meta(NumberModel, { jvmType: "int" }))
  .property("sort", SortModel)
  .build();
type PageableModel = typeof PageableModel;
export default PageableModel;
