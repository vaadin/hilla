import type HierarchyRefSuperclass from "./HierarchyRefSuperclass.js";
interface HierarchyRef extends HierarchyRefSuperclass {
    child?: HierarchyRefSuperclass;
}
export default HierarchyRef;
