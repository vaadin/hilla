import { createRoute as createRoute_1, extendModule as extendModule_1 } from "@vaadin/hilla-file-router/runtime.js";
import type { AgnosticRoute as AgnosticRoute_1 } from "@vaadin/hilla-file-router/types.js";
import { lazy as lazy_1 } from "react";
import * as Page_1 from "../views/test/lazy.js";
const Page_2 = lazy_1(() => import("../issue-2928-flow-auto-layout.js"));
const Page_3 = lazy_1(() => import("../mod-extension-only-child.js"));
const Page_4 = lazy_1(() => import("../views/nameToReplace.js"));
const Page_5 = lazy_1(() => import("../views/profile/@index.js"));
const Layout_1 = lazy_1(() => import("../views/profile/account/@layout.js"));
const Page_6 = lazy_1(() => import("../views/profile/account/security/password.js"));
const Page_7 = lazy_1(() => import("../views/profile/account/security/two-factor-auth.js"));
const Page_8 = lazy_1(() => import("../views/profile/friends/{user}.js"));
const Layout_2 = lazy_1(() => import("../views/profile/friends/@layout.js"));
const Page_9 = lazy_1(() => import("../views/profile/friends/list.js"));
const Page_10 = lazy_1(() => import("../views/test/{...wildcard}.js"));
const Page_11 = lazy_1(() => import("../views/test/{{optional}}.js"));
const Page_12 = lazy_1(() => import("../views/test/issue-002378/{requiredParam}/edit.js"));
const Layout_3 = lazy_1(() => import("../views/test/issue-002571-empty-layout/@layout.js"));
const Page_13 = lazy_1(() => import("../views/test/issue-002879-config-below.js"));
const routes: readonly AgnosticRoute_1[] = [
    createRoute_1("nameToReplace", Page_4),
    createRoute_1("profile", [
        createRoute_1("", Page_5),
        createRoute_1("account", Layout_1, [
            createRoute_1("security", [
                createRoute_1("password", Page_6),
                createRoute_1("two-factor-auth", Page_7)
            ])
        ]),
        createRoute_1("friends", Layout_2, [
            createRoute_1("list", Page_9),
            createRoute_1(":user", Page_8)
        ])
    ]),
    createRoute_1("test", [
        createRoute_1(":optional?", Page_11),
        createRoute_1("*", Page_10),
        createRoute_1("issue-002378", [
            createRoute_1(":requiredParam", [
                createRoute_1("edit", Page_12)
            ])
        ]),
        createRoute_1("issue-002571-empty-layout", Layout_3, []),
        createRoute_1("issue-002879-config-below", Page_13),
        createRoute_1("lazy", Page_1)
    ]),
    createRoute_1("issue-2928-flow-auto-layout", extendModule_1(Page_2, { "flowLayout": true })),
    createRoute_1("mod-extension-only", extendModule_1(null, { "flowLayout": true }), [
        createRoute_1("mod-extension-only-child", Page_3)
    ])
];
export default routes;
