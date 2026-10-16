import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type Notification from "./Notification.js";
class NotificationModel<T extends Notification = Notification> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(NotificationModel);
    get message(): StringModel {
        return this[_getPropertyModel]("message", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default NotificationModel;
