import m from "@vaadin/hilla-models";
import Direction from "./Direction.js";
const DirectionModel = m.enum(Direction, "Direction");
type DirectionModel = typeof DirectionModel;
export default DirectionModel;
