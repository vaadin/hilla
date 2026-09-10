import m, { StringModel } from "@vaadin/hilla-models";
import NotificationModel from "./NotificationModel.js";
import type SmsNotification from "./SmsNotification.js";
const SmsNotificationModel = m
  .extend(NotificationModel)
  .object<SmsNotification>("SmsNotification")
  .property("number", m.meta(m.optional(StringModel), { jvmType: "java.lang.String" }))
  .build();
type SmsNotificationModel = typeof SmsNotificationModel;
export default SmsNotificationModel;
