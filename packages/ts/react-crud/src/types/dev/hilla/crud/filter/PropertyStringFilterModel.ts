import {
    _getPropertyModel as _getPropertyModel_1,
    makeObjectEmptyValueCreator,
    ObjectModel as ObjectModel_1,
    StringModel as StringModel_1
} from "@hilla/form";
import type PropertyStringFilter_1 from "./PropertyStringFilter.js";
import MatcherModel_1 from "./PropertyStringFilter/MatcherModel.js";
class PropertyStringFilterModel<T extends PropertyStringFilter_1 = PropertyStringFilter_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(PropertyStringFilterModel);
    get propertyId(): StringModel_1 {
        return this[_getPropertyModel_1]("propertyId", (parent, key) =>
            new StringModel_1(parent, key, false)
        );
    }
    get filterValue(): StringModel_1 {
        return this[_getPropertyModel_1]("filterValue", (parent, key) =>
            new StringModel_1(parent, key, false)
        );
    }
    get matcher(): MatcherModel_1 {
        return this[_getPropertyModel_1]("matcher", (parent, key) =>
            new MatcherModel_1(parent, key, false)
        );
    }
}
export default PropertyStringFilterModel;
