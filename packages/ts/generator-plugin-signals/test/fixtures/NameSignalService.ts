import { ValueSignal as ValueSignal_1 } from "@vaadin/hilla-react-signals";
import client_1 from "./connect-client.default.js";
import { StringModel } from "@vaadin/hilla-lit-form";
function name_1(options?: {
    defaultValue: string;
}): ValueSignal_1<string> {
    return new ValueSignal_1(options?.defaultValue ?? StringModel.createEmptyValue(), { client: client_1, endpoint: "NameSignalService", method: "name" });
}
export { name_1 as name };
