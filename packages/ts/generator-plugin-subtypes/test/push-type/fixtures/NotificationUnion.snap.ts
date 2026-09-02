import type EmailNotification_1 from "./EmailNotification.js";
import type HtmlEmailNotification_1 from "./HtmlEmailNotification.js";
import type Notification_1 from "./Notification.js";
type NotificationUnion = Notification_1 | EmailNotification_1 | HtmlEmailNotification_1;
export default NotificationUnion;
