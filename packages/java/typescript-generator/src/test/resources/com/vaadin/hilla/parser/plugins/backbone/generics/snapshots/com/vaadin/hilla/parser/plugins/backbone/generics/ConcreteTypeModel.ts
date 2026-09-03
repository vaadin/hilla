import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type ConcreteType_1 from "./ConcreteType.js";
class ConcreteTypeModel<T extends ConcreteType_1 = ConcreteType_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(ConcreteTypeModel);
}
export default ConcreteTypeModel;
