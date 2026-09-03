import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type EmailNotification_1 from "./EmailNotification.js";
import NotificationModel_1 from "./NotificationModel.js";
class EmailNotificationModel<T extends EmailNotification_1 = EmailNotification_1> extends NotificationModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(EmailNotificationModel);
    get address(): StringModel_1 {
        return this[_getPropertyModel_1]("address", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default EmailNotificationModel;
