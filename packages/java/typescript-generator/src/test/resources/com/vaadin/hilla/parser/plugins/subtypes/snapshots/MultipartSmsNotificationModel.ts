import { _getPropertyModel, makeObjectEmptyValueCreator, NumberModel } from "@vaadin/hilla-lit-form";
import type MultipartSmsNotification from "./MultipartSmsNotification.js";
import SmsNotificationModel from "./SmsNotificationModel.js";
class MultipartSmsNotificationModel<T extends MultipartSmsNotification = MultipartSmsNotification> extends SmsNotificationModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(MultipartSmsNotificationModel);
    get parts(): NumberModel {
        return this[_getPropertyModel]("parts", (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "int" } }));
    }
}
export default MultipartSmsNotificationModel;
