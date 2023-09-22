import { _getPropertyModel as _getPropertyModel_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1 } from "@hilla/form";
import type Pageable_1 from "./Pageable.js";
import SortModel_1 from "./SortModel.js";
class PageableModel<T extends Pageable_1 = Pageable_1> extends ObjectModel_1<T> {
    declare static createEmptyValue: () => Pageable_1;
    get pageNumber(): NumberModel_1 {
        return this[_getPropertyModel_1]("pageNumber", NumberModel_1, [false]) as NumberModel_1;
    }
    get pageSize(): NumberModel_1 {
        return this[_getPropertyModel_1]("pageSize", NumberModel_1, [false]) as NumberModel_1;
    }
    get sort(): SortModel_1 {
        return this[_getPropertyModel_1]("sort", SortModel_1, [false]) as SortModel_1;
    }
}
export default PageableModel;
