import type EmailNotification from "./EmailNotification.js";
import type HtmlEmailNotification from "./HtmlEmailNotification.js";
import type MultipartSmsNotification from "./MultipartSmsNotification.js";
import type Notification from "./Notification.js";
type NotificationUnion = (Notification & {
    kind: "plain";
}) | (EmailNotification & {
    kind: "email";
}) | HtmlEmailNotification | MultipartSmsNotification;
export default NotificationUnion;
