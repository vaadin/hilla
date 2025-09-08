import { PageModel as PageModel_1 } from "@vaadin/hilla-frontend";
import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type Custom_1 from "./Custom.js";
class CustomModel<T extends Custom_1 = Custom_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(CustomModel);
    get page(): PageModel_1 {
        return this[_getPropertyModel_1]("page", (parent, key) => new PageModel_1(parent, key, true));
    }
}
export default CustomModel;
