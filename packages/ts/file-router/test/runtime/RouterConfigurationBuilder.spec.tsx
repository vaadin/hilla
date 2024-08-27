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

  it('should add layout routes under layout component', () => {
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
                children: [
                  {
                    path: '/child',
                  },
                ],
                element: createElement(Server),
              },
            ],
            path: '/test',
            handle: {
              flowLayout: true,
            },
          },
        ],
        element: createElement(Server),
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

  it('should not throw when no routes', () => {
    const { routes } = new RouterConfigurationBuilder().withLayout(Server).build();

    expect(routes).to.be.like([]);
  });

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

  it('should protect routes', () => {
    const { routes } = builder.protect('/login').build();

    const [root] = routes;
    const [test] = root.children!;

    expect(protectRoute).to.have.been.calledWith(root, '/login');
    expect(protectRoute).to.have.been.calledWith(test, '/login');
  });

  it('should build the router', () => {
    const { routes, router } = builder.build();

    expect(router).to.equal(browserRouter);
    expect(createBrowserRouter).to.have.been.calledWith(routes, { basename: '/foo' });
    reset();
  });
});
