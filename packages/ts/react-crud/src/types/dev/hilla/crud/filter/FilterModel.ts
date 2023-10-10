import {
    _getPropertyModel as _getPropertyModel_1,
    makeObjectEmptyValueCreator,
    ObjectModel as ObjectModel_1
} from "@hilla/form";
import type Filter_1 from "./Filter.js";
class FilterModel<T extends Filter_1 = Filter_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(FilterModel);
}
export default FilterModel;
