import type Nested_1 from "./Nested.js";
interface Nested {
    numberMatrix?: Array<Array<number | undefined> | undefined>;
    entityMatrix?: Array<Array<Nested_1 | undefined> | undefined>;
    stringMatrix?: Array<Array<string | undefined> | undefined>;
    objectMap?: Record<string, unknown>;
}
export default Nested;
