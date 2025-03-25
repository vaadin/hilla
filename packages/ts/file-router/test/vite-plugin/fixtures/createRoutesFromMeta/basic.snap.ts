import { createLazyModule as createLazyModule_1, createRoute as createRoute_1 } from "@vaadin/hilla-file-router/runtime.js";
import type { AgnosticRoute as AgnosticRoute_1 } from "@vaadin/hilla-file-router/types.js";
import * as Page_1 from "../views/test/non-lazy.js";
const routes: readonly AgnosticRoute_1[] = [
    createRoute_1("nameToReplace", createLazyModule_1(() => import("../views/nameToReplace.js"), { "title": "nameToReplace" })),
    createRoute_1("profile", [
        createRoute_1("", createLazyModule_1(() => import("../views/profile/@index.js"))),
        createRoute_1("account", createLazyModule_1(() => import("../views/profile/account/@layout.js"), { "title": "account" }), [
            createRoute_1("security", [
                createRoute_1("password", createLazyModule_1(() => import("../views/profile/account/security/password.js"), { "title": "password" })),
                createRoute_1("two-factor-auth", createLazyModule_1(() => import("../views/profile/account/security/two-factor-auth.js"), { "title": "two-factor-auth" }))
            ])
        ]),
        createRoute_1("friends", createLazyModule_1(() => import("../views/profile/friends/@layout.js"), { "title": "friends" }), [
            createRoute_1("list", createLazyModule_1(() => import("../views/profile/friends/list.js"), { "title": "list" })),
            createRoute_1(":user", createLazyModule_1(() => import("../views/profile/friends/{user}.js"), { "title": "{user}" }))
        ])
    ]),
    createRoute_1("test", [
        createRoute_1(":optional?", createLazyModule_1(() => import("../views/test/{{optional}}.js"), { "title": "{{optional}}" })),
        createRoute_1("*", createLazyModule_1(() => import("../views/test/{...wildcard}.js"), { "title": "{...wildcard}" })),
        createRoute_1("issue-002378", [
            createRoute_1(":requiredParam", [
                createRoute_1("edit", createLazyModule_1(() => import("../views/test/issue-002378/{requiredParam}/edit.js"), { "title": "edit" }))
            ])
        ]),
        createRoute_1("issue-002571-empty-layout", createLazyModule_1(() => import("../views/test/issue-002571-empty-layout/@layout.js"), { "title": "issue-002571-empty-layout" })),
        createRoute_1("issue-002879-config-below", createLazyModule_1(() => import("../views/test/issue-002879-config-below.js"), { "title": "issue-002879-config-below" })),
        createRoute_1("non-lazy", Page_1)
    ]),
    createRoute_1("issue-2928-flow-auto-layout", createLazyModule_1(() => import("../issue-2928-flow-auto-layout.js"), { "title": "issue-2928-flow-auto-layout" })),
    createRoute_1("mod-extension-only", [
        createRoute_1("mod-extension-only-child", createLazyModule_1(() => import("../mod-extension-only-child.js"), { "title": "mod-extension-only-child" }))
    ])
];
export default routes;
