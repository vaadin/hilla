import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type Pageable_1 from "./Pageable.js";
import SortModel_1 from "./SortModel.js";
class PageableModel<T extends Pageable_1 = Pageable_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(PageableModel);
    get pageNumber(): NumberModel_1 {
        return this[_getPropertyModel_1]("pageNumber", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
    }
    get pageSize(): NumberModel_1 {
        return this[_getPropertyModel_1]("pageSize", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
    }
    get sort(): SortModel_1 {
        return this[_getPropertyModel_1]("sort", (parent, key) => new SortModel_1(parent, key, false));
    }
}
export default PageableModel;
