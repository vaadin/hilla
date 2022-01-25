import { _getPropertyModel as _getPropertyModel_1, NotNull as NotNull_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@vaadin/form";
import type FormEntityId_1 from "./FormEntityId";
class FormEntityIdModel<T extends FormEntityId_1 = FormEntityId_1> extends ObjectModel_1<T> {
    static createEmptyValue: () => FormEntityId_1;
    get Id(): NumberModel_1 {
        return this[_getPropertyModel_1]("Id", NumberModel_1, [true, new NotNull_1()]) as NumberModel_1;
    }
}
export default FormEntityIdModel;
