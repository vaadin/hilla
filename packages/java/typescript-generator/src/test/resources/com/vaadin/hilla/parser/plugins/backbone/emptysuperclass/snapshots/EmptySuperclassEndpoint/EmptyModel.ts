import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type Empty_1 from "./Empty.js";
class EmptyModel<T extends Empty_1 = Empty_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(EmptyModel);
}
export default EmptyModel;
