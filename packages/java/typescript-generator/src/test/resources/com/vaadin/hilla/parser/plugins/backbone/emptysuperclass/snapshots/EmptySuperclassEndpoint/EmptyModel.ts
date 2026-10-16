import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel } from "@vaadin/hilla-lit-form";
import type Empty from "./Empty.js";
class EmptyModel<T extends Empty = Empty> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(EmptyModel);
}
export default EmptyModel;
