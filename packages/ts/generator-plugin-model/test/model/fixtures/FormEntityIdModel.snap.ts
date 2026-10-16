import { _getPropertyModel, makeObjectEmptyValueCreator, NotNull, NumberModel, ObjectModel } from "@vaadin/hilla-lit-form";
import type FormEntityId from "./FormEntityId.js";
class FormEntityIdModel<T extends FormEntityId = FormEntityId> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(FormEntityIdModel);
    get Id(): NumberModel {
        return this[_getPropertyModel]("Id", (parent, key) => new NumberModel(parent, key, false, { validators: [new NotNull()] }));
    }
}
export default FormEntityIdModel;
