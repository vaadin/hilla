import { expect, use } from '@esm-bundle/chai';
import chaiLike from 'chai-like';
import { createElement } from 'react';
import sinonChai from 'sinon-chai';
import { RouterConfigurationBuilder } from '../../src/runtime/RouterConfigurationBuilder.js';
import { mockDocumentBaseURI } from '../mocks/dom.js';
import { browserRouter, createBrowserRouter } from '../mocks/react-router-dom.js';
import { protectRoute } from '../mocks/vaadin-hilla-react-auth.js';

use(chaiLike);
use(sinonChai);

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
  });

  describe('withLayout', () => {
    it('should add layout routes under layout component', () => {
      const { routes } = builder
        .withReactRoutes([
          {
            path: '',
            handle: {
              flowLayout: true,
            },
          },
          {
            path: '/test',
            handle: {
              flowLayout: true,
            },
            children: [
              {
                path: '/child',
                handle: {
                  flowLayout: true,
                },
              },
            ],
          },
        ])
        .withLayout(Server)
        .build();

      expect(routes).to.be.like([
        {
          children: [
            {
              path: '',
              handle: {
                flowLayout: true,
              },
            },
            {
              children: [
                {
                  path: '/child',
                  handle: {
                    flowLayout: true,
                  },
                },
              ],
              path: '/test',
              handle: {
                flowLayout: true,
              },
            },
          ],
          element: createElement(Server),
          handle: {
            ignoreFallback: true,
          },
        },
        {
          children: [
            {
              path: '/test',
              element: <div>Test</div>,
            },
          ],
          path: '',
        },
      ]);
    });

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

    it('should add layout routes for nested folder layout component', () => {
      const { routes } = builder
        .withReactRoutes([
          {
            path: 'nest',
            children: [
              {
                path: '',
                handle: {
                  flowLayout: true,
                },
              },
              {
                path: '/nested',
                handle: {
                  flowLayout: true,
                },
              },
              {
                path: '/outside',
                handle: {
                  flowLayout: false,
                },
              },
              {
                path: '/nested-empty-layout',
                children: [],
              },
            ],
          },
          {
            path: '/test',
            handle: {
              flowLayout: true,
            },
            children: [
              {
                path: '/child',
              },
              {
                path: '/empty-layout',
                children: [],
              },
            ],
          },
          {
            path: '/empty-layout-outside',
            children: [],
          },
        ])
        .withLayout(Server)
        .build();

      expect(routes).to.be.like([
        {
          children: [
            {
              children: [
                {
                  handle: {
                    flowLayout: true,
                  },
                  path: '',
                },
                {
                  handle: {
                    flowLayout: true,
                  },
                  path: '/nested',
                },
              ],
              path: 'nest',
            },
            {
              children: [
                {
                  path: '/child',
                },
                {
                  path: '/empty-layout',
                  children: [],
                },
              ],
              path: '/test',
              handle: {
                flowLayout: true,
              },
            },
          ],
          element: createElement(Server),
          handle: {
            ignoreFallback: true,
          },
        },
        {
          children: [
            {
              path: '/test',
              element: <div>Test</div>,
            },
          ],
          path: '',
        },
        {
          children: [
            {
              path: '/outside',
              handle: {
                flowLayout: false,
              },
            },
            {
              path: '/nested-empty-layout',
              children: [],
            },
          ],
          path: 'nest',
        },
        {
          path: '/empty-layout-outside',
          children: [],
        },
      ]);
    });

    it('should not throw when no routes', () => {
      const { routes } = new RouterConfigurationBuilder().withLayout(Server).build();

      expect(routes).to.be.like([]);
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

  describe('build', () => {
    it('should build the router', () => {
      const { routes, router } = builder.build();

      expect(router).to.equal(browserRouter);
      expect(createBrowserRouter).to.have.been.calledWith(routes, {
        basename: '/foo',
        future: {
          // eslint-disable-next-line camelcase
          v7_fetcherPersist: true,
          // eslint-disable-next-line camelcase
          v7_normalizeFormMethod: true,
          // eslint-disable-next-line camelcase
          v7_partialHydration: true,
          // eslint-disable-next-line camelcase
          v7_relativeSplatPath: true,
          // eslint-disable-next-line camelcase
          v7_skipActionErrorRevalidation: true,
        },
      });
      reset();
    });
  });
});
