import { createBrowserRouter } from 'react-router-dom';
import MainLayout from './MainLayout.js';
import { AutoCrudView } from './_views/AutoCrudView.js';
import { AutoFormView } from './_views/AutoFormView.js';
import { GridWithEntityReferences } from './_views/GridWithEntityReferences.js';
import { ReadOnlyGrid } from './_views/ReadOnlyGrid.js';
import { ReadOnlyGridOrFilter } from './_views/ReadOnlyGridOrFilter.js';
import { ReadOnlyGridSinglePropertyFilter } from './_views/ReadOnlyGridSinglePropertyFilter.js';
import { ReadOnlyGridWithHeaderFilters } from './_views/ReadOnlyGridWithHeaderFilter.js';
import { GridUseDataProviderHook } from './_views/GridUseDataProviderHookView';
import { ReadOnlyGridCustomFilter } from 'Frontend/_views/ReadOnlyGridCustomFilter';

export const routes = [
  {
    path: '',
    element: <MainLayout />,
    children: [
      {
        path: '/readonly-grid',
        element: <ReadOnlyGrid />,
      },
      {
        path: '/readonly-grid-single-property-filter',
        element: <ReadOnlyGridSinglePropertyFilter />,
      },
      {
        path: '/readonly-grid-or-filter',
        element: <ReadOnlyGridOrFilter />,
      },
      {
        path: '/readonly-grid-with-headerfilters',
        element: <ReadOnlyGridWithHeaderFilters />,
      },
      {
        path: '/readonly-grid-custom-filter',
        element: <ReadOnlyGridCustomFilter />,
      },
      {
        path: '/grid-entityrefs',
        element: <GridWithEntityReferences />,
      },
      {
        path: '/grid-use-data-provider-hook',
        element: <GridUseDataProviderHook />,
      },
      {
        path: '/auto-form',
        element: <AutoFormView />,
      },
      {
        path: '/auto-crud',
        element: <AutoCrudView />,
      }
    ],
  },
];

const router = createBrowserRouter([...routes]);
export default router;
