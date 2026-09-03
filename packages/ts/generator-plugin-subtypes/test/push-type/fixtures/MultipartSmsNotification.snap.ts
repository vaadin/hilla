import type SmsNotification_1 from "./SmsNotification.js";
interface MultipartSmsNotification extends SmsNotification_1 {
    parts: number;
    kind: "multipart-sms";
}
export default MultipartSmsNotification;
