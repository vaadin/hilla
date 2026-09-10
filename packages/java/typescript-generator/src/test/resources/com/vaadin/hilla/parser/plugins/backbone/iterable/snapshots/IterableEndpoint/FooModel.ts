import m, { StringModel } from "@vaadin/hilla-models";
import type Foo from "./Foo.js";
const FooModel = m
  .object<Foo>("Foo")
  .property("bar", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type FooModel = typeof FooModel;
export default FooModel;
