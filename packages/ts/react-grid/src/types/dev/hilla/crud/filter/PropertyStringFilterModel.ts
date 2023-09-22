import { _getPropertyModel as _getPropertyModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@hilla/form";
import type PropertyStringFilter_1 from "./PropertyStringFilter.js";
import MatcherModel_1 from "./PropertyStringFilter/MatcherModel.js";
class PropertyStringFilterModel<T extends PropertyStringFilter_1 = PropertyStringFilter_1> extends ObjectModel_1<T> {
    declare static createEmptyValue: () => PropertyStringFilter_1;
    get propertyId(): StringModel_1 {
        return this[_getPropertyModel_1]("propertyId", StringModel_1, [false]) as StringModel_1;
    }
    get filterValue(): StringModel_1 {
        return this[_getPropertyModel_1]("filterValue", StringModel_1, [false]) as StringModel_1;
    }
    get matcher(): MatcherModel_1 {
        return this[_getPropertyModel_1]("matcher", MatcherModel_1, [false]) as MatcherModel_1;
    }
}
export default PropertyStringFilterModel;
