import m from "@vaadin/hilla-models";
import NullHandling from "./NullHandling.js";
const NullHandlingModel = m.enum(NullHandling, "NullHandling");
type NullHandlingModel = typeof NullHandlingModel;
export default NullHandlingModel;
