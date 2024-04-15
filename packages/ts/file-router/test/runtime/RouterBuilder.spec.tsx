import { expect, use } from '@esm-bundle/chai';
import chaiLike from 'chai-like';
import sinonChai from 'sinon-chai';
import { RouterConfigurationBuilder } from '../../src/runtime/RouterConfigurationBuilder.js';
import { browserRouter, createBrowserRouter } from '../mocks/react-router-dom.js';
import { protectRoutes } from '../mocks/vaadin-hilla-react-auth.js';

use(chaiLike);
use(sinonChai);

describe('RouterBuilder', () => {
  let builder: RouterConfigurationBuilder;

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
    builder = new RouterConfigurationBuilder().withReactRoutes({
      path: '',
      children: [
        {
          path: '/test',
          element: <div>Test</div>,
        },
      ],
    });
  });

  it('should merge React routes deeply', () => {
    const { routes } = builder
      .withReactRoutes({
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
      })
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
      .withFileRoutes({
        path: '',
        children: [
          {
            path: '/test',
            module: {
              // eslint-disable-next-line func-name-matching
              default: AltTest,
              config: {
                route: '/alt-test',
              },
            },
          },
          {
            path: '/next-test',
            module: {
              // eslint-disable-next-line func-name-matching
              default: NextTest,
            },
          },
        ],
      })
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
      .withReactRoutes({
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
      })
      .withFallback(Server)
      .build();

    expect(routes).to.be.like([
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
              {
                path: '*',
                element: <Server />,
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
              {
                path: '*',
                element: <Server />,
              },
            ],
          },
          {
            path: '*',
            element: <Server />,
          },
        ],
      },
      {
        path: '*',
        element: <Server />,
      },
    ]);
  });

  it('should protect routes', () => {
    const { routes } = builder
      .withReactRoutes({
        path: '',
        children: [
          {
            path: '/test',
            element: <div>Test</div>,
          },
        ],
      })
      .protect('/login')
      .build();

    expect(protectRoutes).to.have.been.calledWith(routes, '/login');
  });

  it('should build the router', () => {
    const { routes, router } = builder
      .withReactRoutes({
        path: '',
        children: [
          {
            path: '/test',
            element: <div>Test</div>,
          },
        ],
      })
      .build();

    expect(router).to.equal(browserRouter);
    expect(createBrowserRouter).to.have.been.calledWith(routes);
  });
});
