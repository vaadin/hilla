import type Job_1 from "./Job.js";
interface BatchJob extends Job_1 {
    chunkSize: number;
    "@type": "BatchJob";
}
export default BatchJob;
