import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type Notification_1 from "./Notification.js";
class NotificationModel<T extends Notification_1 = Notification_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(NotificationModel);
    get message(): StringModel_1 {
        return this[_getPropertyModel_1]("message", (parent, key) => new StringModel_1(parent, key, true));
    }
}
export default NotificationModel;
