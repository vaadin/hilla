import { _getPropertyModel as _getPropertyModel_1, ObjectModel as ObjectModel_1 } from "@hilla/form";
import type Filter_1 from "./Filter.js";
class FilterModel<T extends Filter_1 = Filter_1> extends ObjectModel_1<T> {
    declare static createEmptyValue: () => Filter_1;
}
export default FilterModel;
