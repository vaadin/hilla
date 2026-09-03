import type EmailNotification_1 from "./EmailNotification.js";
import type SmsNotification_1 from "./SmsNotification.js";
type NotificationUnion = EmailNotification_1 | SmsNotification_1;
export default NotificationUnion;
