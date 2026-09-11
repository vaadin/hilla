import { _getPropertyModel, ArrayModel, makeObjectEmptyValueCreator, ObjectModel } from "@vaadin/hilla-lit-form";
import OrderModel from "./OrderModel.js";
import type Sort from "./Sort.js";
class SortModel<T extends Sort = Sort> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(SortModel);
    get orders(): ArrayModel<OrderModel> {
        return this[_getPropertyModel]("orders", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new OrderModel(parent, key, true), { meta: { javaType: "java.util.List" } }));
    }
}
export default SortModel;
