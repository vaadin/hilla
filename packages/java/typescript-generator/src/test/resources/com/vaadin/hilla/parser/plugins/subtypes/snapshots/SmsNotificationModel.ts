import { _getPropertyModel, makeObjectEmptyValueCreator, StringModel } from "@vaadin/hilla-lit-form";
import NotificationModel from "./NotificationModel.js";
import type SmsNotification from "./SmsNotification.js";
class SmsNotificationModel<T extends SmsNotification = SmsNotification> extends NotificationModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(SmsNotificationModel);
    get number(): StringModel {
        return this[_getPropertyModel]("number", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default SmsNotificationModel;
