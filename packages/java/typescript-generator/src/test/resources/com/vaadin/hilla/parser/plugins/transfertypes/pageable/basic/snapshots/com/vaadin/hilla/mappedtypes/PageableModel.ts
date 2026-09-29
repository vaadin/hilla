import { _getPropertyModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel } from "@vaadin/hilla-lit-form";
import type Pageable from "./Pageable.js";
import SortModel from "./SortModel.js";
class PageableModel<T extends Pageable = Pageable> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(PageableModel);
    get pageNumber(): NumberModel {
        return this[_getPropertyModel]("pageNumber", (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "int" } }));
    }
    get pageSize(): NumberModel {
        return this[_getPropertyModel]("pageSize", (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "int" } }));
    }
    get sort(): SortModel {
        return this[_getPropertyModel]("sort", (parent, key) => new SortModel(parent, key, false));
    }
}
export default PageableModel;
