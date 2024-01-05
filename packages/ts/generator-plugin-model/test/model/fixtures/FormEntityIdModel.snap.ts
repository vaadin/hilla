import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NotNull as NotNull_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type FormEntityId_1 from "./FormEntityId.js";
class FormEntityIdModel<T extends FormEntityId_1 = FormEntityId_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(FormEntityIdModel);
    get Id(): NumberModel_1 {
        return this[_getPropertyModel_1]("Id", (parent, key) => new NumberModel_1(parent, key, false, { validators: [new NotNull_1()] }));
    }
}
export default FormEntityIdModel;
