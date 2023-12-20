import type Filter_1 from "./Filter.js";
interface OrFilter extends Filter_1 {
    children: Array<Filter_1>;
    "@type": "or";
    key: string;
}
export default OrFilter;
