import type SmsNotification from "./SmsNotification.js";
interface MultipartSmsNotification extends SmsNotification {
    parts: number;
    kind: "multipart-sms";
}
export default MultipartSmsNotification;
