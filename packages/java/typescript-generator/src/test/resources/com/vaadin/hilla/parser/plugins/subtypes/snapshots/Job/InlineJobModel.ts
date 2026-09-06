import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import JobModel_1 from "../JobModel.js";
import type InlineJob_1 from "./InlineJob.js";
class InlineJobModel<T extends InlineJob_1 = InlineJob_1> extends JobModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(InlineJobModel);
    get command(): StringModel_1 {
        return this[_getPropertyModel_1]("command", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default InlineJobModel;
