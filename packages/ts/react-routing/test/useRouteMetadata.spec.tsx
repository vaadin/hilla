import { expect } from '@esm-bundle/chai';
import { render } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { type RouteObjectWithMetadata, useRouteMetadata, type RouteMetadata } from '../src';

let metadata: RouteMetadata | undefined;

const CaptureMetadata = () => {
  metadata = useRouteMetadata();
  return <div />;
};

const mainMetadata = {
  title: 'Main route',
  icon: 'main',
  requireAuthentication: true,
  rolesAllowed: ['ROLE_MAIN'],
};
const nestedMetadata = {
  title: 'Nested route',
  icon: 'nested',
  requireAuthentication: true,
  rolesAllowed: ['ROLE_NESTED'],
};

const routes: RouteObjectWithMetadata[] = [
  {
    path: '/main',
    element: <CaptureMetadata />,
    handle: mainMetadata,
    children: [
      {
        path: '/main/with-meta',
        element: <CaptureMetadata />,
        handle: nestedMetadata,
      },
      {
        path: '/main/without-meta',
        element: <CaptureMetadata />,
      },
    ],
  },
  {
    path: '/without-meta',
    element: <CaptureMetadata />,
  },
];

describe('@hilla/react-routing', () => {
  describe('useRouteMetadata', () => {
    it('should return route metadata', () => {
      const router = createMemoryRouter(routes, {
        initialEntries: ['/main'],
      });

      render(<RouterProvider router={router} />);

      expect(metadata).to.exist;
      expect(metadata).to.deep.equal(mainMetadata);
    });

    it('should return undefined if route has no metadata', () => {
      const router = createMemoryRouter(routes, {
        initialEntries: ['/without-meta'],
      });

      render(<RouterProvider router={router} />);

      expect(metadata).to.be.undefined;
    });

    it('should return metadata from nested route', () => {
      const router = createMemoryRouter(routes, {
        initialEntries: ['/main/with-meta'],
      });

      render(<RouterProvider router={router} />);

      expect(metadata).to.exist;
      expect(metadata).to.deep.equal(nestedMetadata);
    });

    it('should return parent metadata if nested route has no metadata', () => {
      const router = createMemoryRouter(routes, {
        initialEntries: ['/main/without-meta'],
      });

      render(<RouterProvider router={router} />);

      expect(metadata).to.exist;
      expect(metadata).to.deep.equal(mainMetadata);
    });
  });
});
