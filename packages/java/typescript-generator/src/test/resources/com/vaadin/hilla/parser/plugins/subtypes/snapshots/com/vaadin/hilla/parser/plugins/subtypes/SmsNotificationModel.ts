import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import NotificationModel_1 from "./NotificationModel.js";
import type SmsNotification_1 from "./SmsNotification.js";
class SmsNotificationModel<T extends SmsNotification_1 = SmsNotification_1> extends NotificationModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(SmsNotificationModel);
    get number(): StringModel_1 {
        return this[_getPropertyModel_1]("number", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default SmsNotificationModel;
