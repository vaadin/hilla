import { createRoute } from "@vaadin/hilla-file-router/runtime.js";
import type { AgnosticRoute, RouteModule } from "@vaadin/hilla-file-router/types.js";
import { lazy } from "react";
import * as Page from "../views/@index.js";
import * as Layout from "../views/@layout.js";
import * as Page_1 from "../views/login.js";
import * as Page_2 from "../views/test/non-lazy.js";
const routes: readonly AgnosticRoute[] = [
    createRoute("", Layout.default, (Layout as RouteModule).config, [
        createRoute("", Page.default, (Page as RouteModule).config),
        createRoute("login", Page_1.default, (Page_1 as RouteModule).config),
        createRoute("nameToReplace", lazy(() => import("../views/nameToReplace.js")), { "title": "nameToReplace" }),
        createRoute("profile", [
            createRoute("", lazy(() => import("../views/profile/@index.js"))),
            createRoute("account", lazy(() => import("../views/profile/account/@layout.js")), { "title": "account" }, [
                createRoute("security", [
                    createRoute("password", lazy(() => import("../views/profile/account/security/password.js")), { "title": "password" }),
                    createRoute("two-factor-auth", lazy(() => import("../views/profile/account/security/two-factor-auth.js")), { "title": "two-factor-auth" })
                ])
            ]),
            createRoute("friends", lazy(() => import("../views/profile/friends/@layout.js")), { "title": "friends" }, [
                createRoute("list", lazy(() => import("../views/profile/friends/list.js")), { "title": "list" }),
                createRoute(":user", lazy(() => import("../views/profile/friends/{user}.js")), { "title": "{user}" })
            ])
        ]),
        createRoute("test", [
            createRoute(":optional?", lazy(() => import("../views/test/{{optional}}.js")), { "title": "{{optional}}" }),
            createRoute("*", lazy(() => import("../views/test/{...wildcard}.js")), { "title": "{...wildcard}" }),
            createRoute("issue-002378", [
                createRoute(":requiredParam", [
                    createRoute("edit", lazy(() => import("../views/test/issue-002378/{requiredParam}/edit.js")), { "title": "edit" })
                ])
            ]),
            createRoute("issue-002571-empty-layout", lazy(() => import("../views/test/issue-002571-empty-layout/@layout.js")), { "title": "issue-002571-empty-layout" }, []),
            createRoute("issue-002879-config-below", lazy(() => import("../views/test/issue-002879-config-below.js")), { "title": "issue-002879-config-below" }),
            createRoute("non-lazy", Page_2.default, (Page_2 as RouteModule).config)
        ])
    ]),
    createRoute("issue-2928-flow-auto-layout", lazy(() => import("../issue-2928-flow-auto-layout.js")), { "title": "issue-2928-flow-auto-layout" }),
    createRoute("mod-extension-only", [
        createRoute("mod-extension-only-child", lazy(() => import("../mod-extension-only-child.js")), { "title": "mod-extension-only-child" })
    ])
];
export default routes;
