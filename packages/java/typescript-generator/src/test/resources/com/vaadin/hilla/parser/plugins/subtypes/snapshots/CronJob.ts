import type RecurringJob_1 from "./RecurringJob.js";
interface CronJob extends RecurringJob_1 {
    expression?: string;
    "@type": "cron";
}
export default CronJob;
