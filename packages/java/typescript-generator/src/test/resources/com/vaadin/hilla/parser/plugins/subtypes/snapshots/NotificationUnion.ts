import type EmailNotification_1 from "./EmailNotification.js";
import type HtmlEmailNotification_1 from "./HtmlEmailNotification.js";
import type MultipartSmsNotification_1 from "./MultipartSmsNotification.js";
import type Notification_1 from "./Notification.js";
type NotificationUnion = (Notification_1 & {
    kind: "plain";
}) | (EmailNotification_1 & {
    kind: "email";
}) | HtmlEmailNotification_1 | MultipartSmsNotification_1;
export default NotificationUnion;
