import chaiLike from 'chai-like';
import { createElement } from 'react';
import sinonChai from 'sinon-chai';
import { expect, chai, describe, it, beforeEach, afterEach } from 'vitest';
import { RouterConfigurationBuilder } from '../../src/runtime/RouterConfigurationBuilder.js';
import { mockDocumentBaseURI } from '../mocks/dom.js';
import { protectRoute } from '../mocks/vaadin-hilla-react-auth.js';

chai.use(chaiLike);
chai.use(sinonChai);

describe('RouterBuilder', () => {
  let builder: RouterConfigurationBuilder;
  let reset: () => void;

  function Index() {
    return <></>;
  }

  function NextTest() {
    return <></>;
  }

  function AltTest() {
    return <></>;
  }

  function Server() {
    return <></>;
  }

  function Layout() {
    return <></>;
  }

  beforeEach(() => {
    builder = new RouterConfigurationBuilder().withReactRoutes([
      {
        path: '',
        children: [
          {
            path: '/test',
            element: <div>Test</div>,
          },
        ],
      },
    ]);
    reset = mockDocumentBaseURI('https://example.com/foo');
    globalThis.window = {
      // @ts-expect-error Fake just enough so tests pass
      history: {
        replaceState: () => {},
      },
      // @ts-expect-error Fake just enough so tests pass
      location: '',
      addEventListener: () => {},
    };
    // @ts-expect-error: Fake just enough so tests pass
    globalThis.document.defaultView = globalThis.window;
  });

  afterEach(() => {
    reset();
  });

  describe('withReactRoutes', () => {
    it('should merge React routes deeply', () => {
      const { routes } = builder
        .withReactRoutes([
          {
            path: '',
            children: [
              {
                path: '/test',
                element: <div>AlternatedTest</div>,
              },
              {
                path: '/next-test',
                element: <div>NextTest</div>,
              },
            ],
          },
        ])
        .build();

      expect(routes).to.be.like([
        {
          path: '',
          children: [
            {
              path: '/test',
              element: <div>AlternatedTest</div>,
            },
            {
              path: '/next-test',
              element: <div>NextTest</div>,
            },
          ],
        },
      ]);
    });

    it('should add server routes to children deeply', () => {
      const { routes } = builder
        .withReactRoutes([
          {
            path: '',
            children: [
              {
                path: '/test',
                children: [
                  {
                    path: '/child-test',
                    element: <div>ChildTest</div>,
                  },
                ],
              },
              {
                path: '/next-test',
                children: [
                  {
                    path: '/next-child-test',
                    element: <div>ChildTest</div>,
                  },
                ],
              },
              {
                path: '/cases',
                children: [
                  {
                    path: '/index',
                    children: [
                      {
                        index: true,
                        element: <div>Index</div>,
                      },
                    ],
                  },
                  {
                    path: '/wildcard',
                    children: [
                      {
                        path: '*',
                        element: <div>Wildcard</div>,
                      },
                    ],
                  },
                  {
                    path: '/optional',
                    children: [
                      {
                        path: ':optional?',
                        element: <div>Optional</div>,
                      },
                    ],
                  },
                ],
              },
            ],
          },
        ])
        .withFallback(Server, { title: 'Server' })
        .build();

      const serverWildcard = {
        path: '*',
        element: <Server />,
        handle: { title: 'Server' },
      };
      const serverIndex = {
        index: true,
        element: <Server />,
        handle: { title: 'Server' },
      };

      const serverRoutes = [serverWildcard, serverIndex];

      expect(routes).to.be.like([
        {
          path: '',
          children: [
            {
              path: '/test',
              element: <div>Test</div>,
            },
            {
              path: '/test',
              children: [
                {
                  path: '/child-test',
                  element: <div>ChildTest</div>,
                },
                ...serverRoutes,
              ],
            },
            {
              path: '/next-test',
              children: [
                {
                  path: '/next-child-test',
                  element: <div>ChildTest</div>,
                },
                ...serverRoutes,
              ],
            },
            {
              path: '/cases',
              children: [
                // If we already have an index route, all we want is to add a
                // server wildcard
                {
                  path: '/index',
                  children: [
                    {
                      index: true,
                      element: <div>Index</div>,
                    },
                    serverWildcard,
                  ],
                },
                // If we already have a wildcard route, all we want is to add a
                // server index.
                {
                  path: '/wildcard',
                  children: [
                    {
                      path: '*',
                      element: <div>Wildcard</div>,
                    },
                    serverIndex,
                  ],
                },
                // If we have an optional route, we just need to add a server
                // wildcard to cover complicated cases like
                // "/optional/something/else/deeply/nested"
                {
                  path: '/optional',
                  children: [
                    {
                      path: ':optional?',
                      element: <div>Optional</div>,
                    },
                    serverWildcard,
                  ],
                },
                ...serverRoutes,
              ],
            },
            ...serverRoutes,
          ],
        },
        ...serverRoutes,
      ]);
    });
  });

  describe('withFileRoutes', () => {
    it('should merge file routes deeply', () => {
      const { routes } = builder
        .withFileRoutes([
          {
            path: '',
            children: [
              {
                path: '',
                module: {
                  default: Index,
                },
              },
              {
                path: '/test',
                module: {
                  default: AltTest,
                  config: {
                    route: '/alt-test',
                  },
                },
              },
              {
                path: '/next-test',
                module: {
                  default: NextTest,
                },
              },
            ],
          },
        ])
        .build();

      expect(routes).to.be.like([
        {
          path: '',
          children: [
            {
              path: '/alt-test',
              element: <AltTest />,
              handle: {
                title: 'Alt Test',
                route: '/alt-test',
              },
            },
            {
              element: <Index />,
              handle: {
                title: 'Index',
              },
              index: true,
            },
            {
              path: '/next-test',
              element: <NextTest />,
              handle: {
                title: 'Next Test',
              },
            },
          ],
        },
      ]);
    });

    it('should accept a file route module with a config only', () => {
      expect(() =>
        builder.withFileRoutes([{ path: 'test', module: { config: { flowLayout: true } } }]).build(),
      ).to.not.throw();
    });

    it('should accept an undefined file route module', () => {
      expect(() => builder.withFileRoutes([{ path: 'test', module: undefined }]).build()).to.not.throw();
    });

    it('should throw if a file route module has no component or config', () => {
      expect(() => builder.withFileRoutes([{ path: 'test', module: {} }]).build()).to.throw(
        `The module for the "test" section doesn't have the React component exported by default or a ViewConfig object exported as "config"`,
      );
    });

    it('should not lose a route from the result', () => {
      const { routes } = builder
        .withReactRoutes([
          {
            path: 'home',
            children: [
              {
                path: 'deep',
                children: [
                  {
                    path: 'hello',
                    handle: {
                      flowLayout: true,
                    },
                  },
                ],
                handle: { flowLayout: true },
              },
              {
                path: 'deepend',
                children: [{ path: 'deep' }],
              },
            ],
          },
        ])
        .withLayout(Server)
        .build();

      expect(routes).to.be.like([
        {
          element: createElement(Server),
          handle: {
            ignoreFallback: true,
          },
          children: [
            {
              path: 'home',
              children: [
                {
                  path: 'deep',
                  children: [
                    {
                      path: 'hello',
                      handle: {
                        flowLayout: true,
                      },
                    },
                  ],
                  handle: { flowLayout: true },
                },
              ],
            },
          ],
        },
        {
          path: '',
          children: [
            {
              path: '/test',
              element: <div>Test</div>,
            },
          ],
        },
        {
          path: 'home',
          children: [
            {
              path: 'deepend',
              children: [{ path: 'deep' }],
            },
          ],
        },
      ]);
    });
  });

  describe('withLayout', () => {
    it('empty flowLayout array should not generate element', () => {
      const { routes } = new RouterConfigurationBuilder()
        .withReactRoutes([
          {
            path: '',
          },
        ])
        .withLayout(Server)
        .build();

      expect(routes).to.be.like([
        {
          path: '',
        },
      ]);
    });

    it('should separate flow layouts and regular layouts', () => {
      const { routes } = builder
        .withReactRoutes([
          {
            path: 'mixed-nested-layouts',
            children: [
              {
                path: 'parent-flow-layout',
                handle: {
                  flowLayout: true,
                },
                children: [
                  {
                    path: 'flow-layout',
                    handle: {
                      flowLayout: true,
                    },
                  },
                  {
                    path: 'regular-layout-1',
                    handle: {
                      flowLayout: false,
                    },
                  },
                  {
                    path: 'not-specified-layout-1',
                  },
                  {
                    path: 'not-specified-layout-2',
                    children: [
                      {
                        path: 'child-regular-layout',
                        handle: {
                          flowLayout: false,
                        },
                      },
                    ],
                  },
                  {
                    path: 'not-specified-layout-3',
                    children: [
                      {
                        path: 'child-not-specifed-layout',
                      },
                    ],
                  },
                  {
                    path: 'not-specified-layout-4',
                    children: [
                      {
                        path: 'child-flow-layout-1',
                        handle: {
                          flowLayout: true,
                        },
                      },
                    ],
                  },
                  {
                    path: 'regular-layout-2',
                    handle: {
                      flowLayout: false,
                    },
                    children: [
                      {
                        path: 'child-flow-layout-2',
                        handle: {
                          flowLayout: true,
                        },
                      },
                    ],
                  },
                ],
              },
            ],
          },
          {
            path: 'flow-layout-another-branch',
            handle: {
              flowLayout: true,
            },
          },
          {
            path: 'regular-layout-outside',
            handle: {
              flowLayout: false,
            },
          },
          {
            path: 'not-specified-layout-outside',
          },
        ])
        .withLayout(Server)
        .build();

      expect(routes).to.be.like([
        {
          element: createElement(Server),
          handle: {
            ignoreFallback: true,
          },
          children: [
            {
              path: 'mixed-nested-layouts',
              children: [
                {
                  path: 'parent-flow-layout',
                  handle: {
                    flowLayout: true,
                  },
                  children: [
                    {
                      path: 'flow-layout',
                      handle: {
                        flowLayout: true,
                      },
                    },
                    {
                      path: 'not-specified-layout-4',
                      children: [
                        {
                          path: 'child-flow-layout-1',
                          handle: {
                            flowLayout: true,
                          },
                        },
                      ],
                    },
                    {
                      path: 'regular-layout-2',
                      handle: {
                        flowLayout: false,
                      },
                      children: [
                        {
                          path: 'child-flow-layout-2',
                          handle: {
                            flowLayout: true,
                          },
                        },
                      ],
                    },
                    {
                      path: 'not-specified-layout-1',
                    },
                    {
                      path: 'not-specified-layout-2',
                    },
                    {
                      path: 'not-specified-layout-3',
                      children: [
                        {
                          path: 'child-not-specifed-layout',
                        },
                      ],
                    },
                  ],
                },
              ],
            },
            {
              path: 'flow-layout-another-branch',
              handle: {
                flowLayout: true,
              },
            },
          ],
        },
        {
          path: 'mixed-nested-layouts',
          children: [
            {
              path: 'parent-flow-layout',
              handle: {
                flowLayout: true,
              },
              children: [
                {
                  path: 'regular-layout-1',
                  handle: {
                    flowLayout: false,
                  },
                },
                {
                  path: 'not-specified-layout-2',
                  children: [
                    {
                      path: 'child-regular-layout',
                      handle: {
                        flowLayout: false,
                      },
                    },
                  ],
                },
                {
                  path: 'regular-layout-2',
                  handle: {
                    flowLayout: false,
                  },
                },
              ],
            },
          ],
        },
        {
          path: 'regular-layout-outside',
          handle: {
            flowLayout: false,
          },
        },
        {
          path: '',
          children: [
            {
              path: '/test',
              element: <div>Test</div>,
            },
          ],
        },
        {
          path: 'not-specified-layout-outside',
        },
      ]);
    });
  });

  describe('withLayoutSkipping', () => {
    it('should skip layout routes', () => {
      const { routes } = builder
        .withReactRoutes([
          {
            path: '/shallow-skip',
            handle: {
              skipLayouts: true,
            },
          },
          {
            path: '/deep-skip',
            children: [
              {
                path: '/deep-skip-1',
                children: [
                  {
                    path: '/deep-skip-2',
                    handle: {
                      skipLayouts: true,
                    },
                  },
                  {
                    path: '/deep-skip-excluded-1',
                  },
                ],
              },
              {
                path: '/deep-skip-excluded-2',
              },
            ],
          },
          {
            path: '/flow-skip',
            handle: {
              flowLayout: true,
            },
            children: [
              {
                path: '/flow-skip-1',
                handle: {
                  skipLayouts: true,
                },
              },
              {
                path: '/flow-skip-sibling',
              },
            ],
          },
        ])
        .withLayout(Server)
        .build();

      expect(routes).to.be.like([
        {
          handle: {
            ignoreFallback: true,
          },
          children: [
            {
              handle: {
                ignoreFallback: true,
              },
              children: [
                {
                  path: '/flow-skip',
                  handle: {
                    flowLayout: true,
                  },
                  children: [
                    {
                      path: '/flow-skip-1',
                      handle: {
                        skipLayouts: true,
                      },
                    },
                  ],
                },
              ],
            },
            {
              path: '/shallow-skip',
              handle: {
                skipLayouts: true,
              },
            },
            {
              path: '/deep-skip',
              children: [
                {
                  path: '/deep-skip-1',
                  children: [
                    {
                      path: '/deep-skip-2',
                      handle: { skipLayouts: true },
                    },
                  ],
                },
              ],
            },
          ],
        },
        {
          element: createElement(Server),
          handle: {
            ignoreFallback: true,
          },
          children: [
            {
              path: '/flow-skip',
              handle: {
                flowLayout: true,
              },
              children: [
                {
                  path: '/flow-skip-sibling',
                },
              ],
            },
          ],
        },
        {
          path: '',
          children: [
            {
              path: '/test',
              element: <div>Test</div>,
            },
          ],
        },
        {
          path: '/deep-skip',
          children: [
            {
              path: '/deep-skip-1',
              children: [
                {
                  path: '/deep-skip-excluded-1',
                },
              ],
            },
            {
              path: '/deep-skip-excluded-2',
            },
          ],
        },
      ]);
    });
  });

  describe('protect', () => {
    it('should protect routes', () => {
      const { routes } = builder.protect('/login').build();

      const [root] = routes;
      const [test] = root.children!;

      expect(protectRoute).to.have.been.calledWith(root, '/login');
      expect(protectRoute).to.have.been.calledWith(test, '/login');
    });
  });

  describe('issues', () => {
    it('#2954', () => {
      const { routes } = new RouterConfigurationBuilder()
        .withFileRoutes([
          {
            path: '',
            children: [
              {
                path: '',
                module: {
                  config: {
                    menu: { order: 0 },
                    title: 'Public view',
                  },
                  default: NextTest,
                },
              },
              {
                path: 'login',
                module: {
                  config: {
                    menu: { exclude: true },
                    flowLayout: false,
                  },
                },
              },
            ],
          },
        ])
        .withFallback(Server)
        .protect()
        .build();

      expect(routes).to.be.like([
        {
          path: '',
          children: [
            {
              path: 'login',
              handle: {
                menu: {
                  exclude: true,
                },
                flowLayout: false,
                title: 'undefined',
              },
            },
          ],
          handle: {
            title: 'undefined',
          },
        },
        {
          path: '',
          children: [
            {
              element: <NextTest />,
              handle: {
                menu: { order: 0 },
                title: 'Public view',
              },
              index: true,
            },
            { path: '*', element: <Server /> },
          ],
          handle: { title: 'undefined' },
        },
        { path: '*', element: <Server /> },
        { index: true, element: <Server /> },
      ]);
    });
  });

  describe('combinations', () => {
    it('should support file routes with server layout and fallback', () => {
      const { routes } = new RouterConfigurationBuilder()
        .withFileRoutes([
          {
            path: '/next-test',
            module: {
              default: NextTest,
              config: {
                flowLayout: true,
              },
            },
          },
        ])
        .withFallback(Server)
        .build();

      expect(routes).to.be.like([
        {
          element: <Server />,
          handle: {
            ignoreFallback: true,
          },
          children: [{ path: '/next-test', element: <NextTest /> }],
        },
        { path: '*', element: <Server /> },
        { index: true, element: <Server /> },
      ]);
    });
  });

  it('should support file routes with only client layout and server fallback', () => {
    const { routes } = new RouterConfigurationBuilder()
      .withFileRoutes([
        {
          path: '',
          module: {
            default: Layout,
          },
          children: [],
        },
      ])
      .withFallback(Server)
      .build();

    expect(routes).to.be.like([
      {
        path: '',
        handle: {
          title: 'Layout',
        },
        children: [
          { path: '*', element: <Server /> },
          { index: true, element: <Server /> },
        ],
      },
      { path: '*', element: <Server /> },
      { index: true, element: <Server /> },
    ]);
  });
});
