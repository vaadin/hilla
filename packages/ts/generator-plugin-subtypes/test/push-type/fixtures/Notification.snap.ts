interface Notification {
    message?: string;
    kind: "plain" | "email" | "html-email" | "multipart-sms";
}
export default Notification;
