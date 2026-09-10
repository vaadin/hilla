import { _getPropertyModel, makeObjectEmptyValueCreator, StringModel } from "@vaadin/hilla-lit-form";
import EmailNotificationModel from "./EmailNotificationModel.js";
import type HtmlEmailNotification from "./HtmlEmailNotification.js";
class HtmlEmailNotificationModel<T extends HtmlEmailNotification = HtmlEmailNotification> extends EmailNotificationModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(HtmlEmailNotificationModel);
    get html(): StringModel {
        return this[_getPropertyModel]("html", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default HtmlEmailNotificationModel;
