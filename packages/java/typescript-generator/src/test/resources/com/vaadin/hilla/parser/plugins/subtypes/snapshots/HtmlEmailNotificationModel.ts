import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import EmailNotificationModel_1 from "./EmailNotificationModel.js";
import type HtmlEmailNotification_1 from "./HtmlEmailNotification.js";
class HtmlEmailNotificationModel<T extends HtmlEmailNotification_1 = HtmlEmailNotification_1> extends EmailNotificationModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(HtmlEmailNotificationModel);
    get html(): StringModel_1 {
        return this[_getPropertyModel_1]("html", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default HtmlEmailNotificationModel;
