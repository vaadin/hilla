import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, ObjectModel as ObjectModel_1 } from "@hilla/form";
import type Order_1 from "./Order.js";
import OrderModel_1 from "./OrderModel.js";
import type Sort_1 from "./Sort.js";
class SortModel<T extends Sort_1 = Sort_1> extends ObjectModel_1<T> {
    declare static createEmptyValue: () => Sort_1;
    get orders(): ArrayModel_1<Order_1, OrderModel_1> {
        return this[_getPropertyModel_1]("orders", ArrayModel_1, [false, OrderModel_1, [true]]) as ArrayModel_1<Order_1, OrderModel_1>;
    }
}
export default SortModel;
