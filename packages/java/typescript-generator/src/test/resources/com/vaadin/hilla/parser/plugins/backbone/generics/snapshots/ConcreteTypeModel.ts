import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel } from "@vaadin/hilla-lit-form";
import type ConcreteType from "./ConcreteType.js";
class ConcreteTypeModel<T extends ConcreteType = ConcreteType> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(ConcreteTypeModel);
}
export default ConcreteTypeModel;
