import { expect, use } from '@esm-bundle/chai';
import chaiLike from 'chai-like';
import sinonChai from 'sinon-chai';
import { RouterBuilder } from '../../src/runtime/RouterBuilder.js';
import { browserRouter, createBrowserRouter } from '../mocks/react-router-dom.js';
import { protectRoutes } from '../mocks/vaadin-hilla-react-auth.js';

use(chaiLike);
use(sinonChai);

describe('RouterBuilder', () => {
  let routerBuilder: RouterBuilder;

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
    routerBuilder = new RouterBuilder().withReactRoutes({
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
    routerBuilder.withReactRoutes({
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
    });

    expect(routerBuilder.routes).to.be.like([
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
    routerBuilder.withFileRoutes({
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
    });

    expect(routerBuilder.routes).to.be.like([
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
    routerBuilder.withReactRoutes({
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
    });
    routerBuilder.withServerRoutes(Server);

    expect(routerBuilder.routes).to.be.like([
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
    routerBuilder.withReactRoutes({
      path: '',
      children: [
        {
          path: '/test',
          element: <div>Test</div>,
        },
      ],
    });
    routerBuilder.protect('/login');
    expect(protectRoutes).to.have.been.calledWith(routerBuilder.routes, '/login');
  });

  it('should build the router', () => {
    routerBuilder.withReactRoutes({
      path: '',
      children: [
        {
          path: '/test',
          element: <div>Test</div>,
        },
      ],
    });
    const router = routerBuilder.build();
    expect(router).to.equal(browserRouter);
    expect(createBrowserRouter).to.have.been.calledWith(routerBuilder.routes);
  });
});
