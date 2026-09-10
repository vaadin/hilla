import m, { StringModel } from "@vaadin/hilla-models";
import type Notification from "./Notification.js";
const NotificationModel = m
  .object<Notification>("Notification")
  .property("message", m.optional(StringModel))
  .property("kind", m.union(m.literal("plain"), m.literal("email"), m.literal("html-email"), m.literal("multipart-sms")))
  .build();
type NotificationModel = typeof NotificationModel;
export default NotificationModel;
