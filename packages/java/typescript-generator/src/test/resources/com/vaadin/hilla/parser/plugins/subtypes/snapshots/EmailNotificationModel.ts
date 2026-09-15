import { _getPropertyModel, makeObjectEmptyValueCreator, StringModel } from "@vaadin/hilla-lit-form";
import type EmailNotification from "./EmailNotification.js";
import NotificationModel from "./NotificationModel.js";
class EmailNotificationModel<T extends EmailNotification = EmailNotification> extends NotificationModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(EmailNotificationModel);
    get address(): StringModel {
        return this[_getPropertyModel]("address", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default EmailNotificationModel;
