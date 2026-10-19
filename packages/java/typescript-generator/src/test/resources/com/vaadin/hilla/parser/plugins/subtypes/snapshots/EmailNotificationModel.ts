import m, { StringModel } from "@vaadin/hilla-models";
import type EmailNotification from "./EmailNotification.js";
import NotificationModel from "./NotificationModel.js";
const EmailNotificationModel = m
  .extend(NotificationModel)
  .object<EmailNotification>("EmailNotification")
  .property("address", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .property("kind", m.union(m.literal("email"), m.literal("html-email")))
  .build();
type EmailNotificationModel = typeof EmailNotificationModel;
export default EmailNotificationModel;
