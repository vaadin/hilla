import { expect, use } from '@esm-bundle/chai';
import chaiLike from 'chai-like';
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
          ],
        },
      ])
      .withFallback(Server, { title: 'Server' })
      .build();

    const serverRoutes = [
      {
        path: '*',
        element: <Server />,
        handle: { title: 'Server' },
      },
      {
        index: true,
        element: <Server />,
        handle: { title: 'Server' },
      },
    ];

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
