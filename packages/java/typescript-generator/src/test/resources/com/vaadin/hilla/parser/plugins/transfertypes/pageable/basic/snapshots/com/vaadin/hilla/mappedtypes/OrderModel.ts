import { _getPropertyModel, BooleanModel, makeObjectEmptyValueCreator, NotBlank, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import DirectionModel from "../../../../org/springframework/data/domain/Sort/DirectionModel.js";
import NullHandlingModel from "../../../../org/springframework/data/domain/Sort/NullHandlingModel.js";
import type Order from "./Order.js";
class OrderModel<T extends Order = Order> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(OrderModel);
    get direction(): DirectionModel {
        return this[_getPropertyModel]("direction", (parent, key) => new DirectionModel(parent, key, false));
    }
    get property(): StringModel {
        return this[_getPropertyModel]("property", (parent, key) => new StringModel(parent, key, false, { validators: [new NotBlank()], meta: { javaType: "java.lang.String" } }));
    }
    get ignoreCase(): BooleanModel {
        return this[_getPropertyModel]("ignoreCase", (parent, key) => new BooleanModel(parent, key, false, { meta: { javaType: "boolean" } }));
    }
    get nullHandling(): NullHandlingModel {
        return this[_getPropertyModel]("nullHandling", (parent, key) => new NullHandlingModel(parent, key, true));
    }
}
export default OrderModel;
