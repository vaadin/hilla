import m from "@vaadin/hilla-models";
import type ConcreteType from "./ConcreteType.js";
const ConcreteTypeModel = m.object<ConcreteType>("ConcreteType").build();
type ConcreteTypeModel = typeof ConcreteTypeModel;
export default ConcreteTypeModel;
