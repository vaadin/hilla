import m, { NumberModel } from "@vaadin/hilla-models";
import type MultipartSmsNotification from "./MultipartSmsNotification.js";
import SmsNotificationModel from "./SmsNotificationModel.js";
const MultipartSmsNotificationModel = m
  .extend(SmsNotificationModel)
  .object<MultipartSmsNotification>("MultipartSmsNotification")
  .property("parts", m.meta(NumberModel, { jvmType: "int" }))
  .property("kind", m.literal("multipart-sms"))
  .build();
type MultipartSmsNotificationModel = typeof MultipartSmsNotificationModel;
export default MultipartSmsNotificationModel;
