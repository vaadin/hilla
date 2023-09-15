import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, ObjectModel as ObjectModel_1 } from "@hilla/form";
import type Filter_1 from "./Filter.js";
import FilterModel_1 from "./FilterModel.js";
import type OrFilter_1 from "./OrFilter.js";
class OrFilterModel<T extends OrFilter_1 = OrFilter_1> extends ObjectModel_1<T> {
    declare static createEmptyValue: () => OrFilter_1;
    get children(): ArrayModel_1<Filter_1, FilterModel_1> {
        return this[_getPropertyModel_1]("children", ArrayModel_1, [false, FilterModel_1, [false]]) as ArrayModel_1<Filter_1, FilterModel_1>;
    }
}
export default OrFilterModel;
