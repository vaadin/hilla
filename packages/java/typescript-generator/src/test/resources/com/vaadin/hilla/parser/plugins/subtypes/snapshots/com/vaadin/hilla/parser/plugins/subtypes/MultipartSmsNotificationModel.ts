import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1 } from "@vaadin/hilla-lit-form";
import type MultipartSmsNotification_1 from "./MultipartSmsNotification.js";
import SmsNotificationModel_1 from "./SmsNotificationModel.js";
class MultipartSmsNotificationModel<T extends MultipartSmsNotification_1 = MultipartSmsNotification_1> extends SmsNotificationModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(MultipartSmsNotificationModel);
    get parts(): NumberModel_1 {
        return this[_getPropertyModel_1]("parts", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
    }
}
export default MultipartSmsNotificationModel;
