import {
    _getPropertyModel as _getPropertyModel_1,
    makeObjectEmptyValueCreator,
    NumberModel as NumberModel_1,
    ObjectModel as ObjectModel_1
} from "@hilla/form";
import type Pageable_1 from "./Pageable.js";
import SortModel_1 from "./SortModel.js";
class PageableModel<T extends Pageable_1 = Pageable_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(PageableModel);
    get pageNumber(): NumberModel_1 {
        return this[_getPropertyModel_1]("pageNumber", (parent, key) =>
            new NumberModel_1(parent, key, false)
        );
    }
    get pageSize(): NumberModel_1 {
        return this[_getPropertyModel_1]("pageSize", (parent, key) =>
            new NumberModel_1(parent, key, false)
        );
    }
    get sort(): SortModel_1 {
        return this[_getPropertyModel_1]("sort", (parent, key) =>
            new SortModel_1(parent, key, false)
        );
    }
}
export default PageableModel;
