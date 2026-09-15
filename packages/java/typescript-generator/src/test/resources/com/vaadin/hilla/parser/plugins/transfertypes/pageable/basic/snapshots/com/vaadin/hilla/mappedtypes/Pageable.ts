import type Sort from "./Sort.js";
interface Pageable {
    pageNumber: number;
    pageSize: number;
    sort: Sort;
}
export default Pageable;
