import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1 } from "@vaadin/hilla-lit-form";
import type BatchJob_1 from "./BatchJob.js";
import JobModel_1 from "./JobModel.js";
class BatchJobModel<T extends BatchJob_1 = BatchJob_1> extends JobModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(BatchJobModel);
    get chunkSize(): NumberModel_1 {
        return this[_getPropertyModel_1]("chunkSize", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
    }
}
export default BatchJobModel;
