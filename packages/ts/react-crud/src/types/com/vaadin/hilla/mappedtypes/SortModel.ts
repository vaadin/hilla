import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import OrderModel_1 from "./OrderModel.js";
import type Sort_1 from "./Sort.js";
class SortModel<T extends Sort_1 = Sort_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(SortModel);
    get orders(): ArrayModel_1<OrderModel_1> {
        return this[_getPropertyModel_1]("orders", (parent, key) => new ArrayModel_1(parent, key, false, (parent, key) => new OrderModel_1(parent, key, true), { meta: { javaType: "java.util.List" } }));
    }
}
export default SortModel;
