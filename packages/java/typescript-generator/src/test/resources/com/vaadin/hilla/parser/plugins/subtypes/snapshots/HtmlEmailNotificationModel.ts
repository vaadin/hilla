import m, { StringModel } from "@vaadin/hilla-models";
import EmailNotificationModel from "./EmailNotificationModel.js";
import type HtmlEmailNotification from "./HtmlEmailNotification.js";
const HtmlEmailNotificationModel = m
  .extend(EmailNotificationModel)
  .object<HtmlEmailNotification>("HtmlEmailNotification")
  .property("html", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("kind", m.literal("html-email"))
  .build();
type HtmlEmailNotificationModel = typeof HtmlEmailNotificationModel;
export default HtmlEmailNotificationModel;
