import { _getPropertyModel as _getPropertyModel_1, BooleanModel as BooleanModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NotBlank as NotBlank_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import DirectionModel_1 from "../../../../org/springframework/data/domain/Sort/DirectionModel.js";
import NullHandlingModel_1 from "../../../../org/springframework/data/domain/Sort/NullHandlingModel.js";
import type Order_1 from "./Order.js";
class OrderModel<T extends Order_1 = Order_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(OrderModel);
    get direction(): DirectionModel_1 {
        return this[_getPropertyModel_1]("direction", (parent, key) => new DirectionModel_1(parent, key, false));
    }
    get property(): StringModel_1 {
        return this[_getPropertyModel_1]("property", (parent, key) => new StringModel_1(parent, key, false, { validators: [new NotBlank_1()], meta: { javaType: "java.lang.String" } }));
    }
    get ignoreCase(): BooleanModel_1 {
        return this[_getPropertyModel_1]("ignoreCase", (parent, key) => new BooleanModel_1(parent, key, false, { meta: { javaType: "boolean" } }));
    }
    get nullHandling(): NullHandlingModel_1 {
        return this[_getPropertyModel_1]("nullHandling", (parent, key) => new NullHandlingModel_1(parent, key, true));
    }
}
export default OrderModel;
