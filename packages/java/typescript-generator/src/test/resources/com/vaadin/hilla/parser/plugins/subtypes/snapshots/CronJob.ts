import type Job_1 from "./Job.js";
interface CronJob extends Job_1 {
    expression?: string;
    "@type": "cron";
}
export default CronJob;
