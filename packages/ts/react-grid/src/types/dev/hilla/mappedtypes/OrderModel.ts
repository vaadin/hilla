import { _getPropertyModel as _getPropertyModel_1, BooleanModel as BooleanModel_1, NotBlank as NotBlank_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@hilla/form";
import DirectionModel_1 from "../../../org/springframework/data/domain/Sort/DirectionModel.js";
import NullHandlingModel_1 from "../../../org/springframework/data/domain/Sort/NullHandlingModel.js";
import type Order_1 from "./Order.js";
class OrderModel<T extends Order_1 = Order_1> extends ObjectModel_1<T> {
    declare static createEmptyValue: () => Order_1;
    get direction(): DirectionModel_1 {
        return this[_getPropertyModel_1]("direction", DirectionModel_1, [false]) as DirectionModel_1;
    }
    get property(): StringModel_1 {
        return this[_getPropertyModel_1]("property", StringModel_1, [false, new NotBlank_1()]) as StringModel_1;
    }
    get ignoreCase(): BooleanModel_1 {
        return this[_getPropertyModel_1]("ignoreCase", BooleanModel_1, [false]) as BooleanModel_1;
    }
    get nullHandling(): NullHandlingModel_1 {
        return this[_getPropertyModel_1]("nullHandling", NullHandlingModel_1, [true]) as NullHandlingModel_1;
    }
}
export default OrderModel;
