import sinon from 'sinon';

export const protectRoutes = sinon.stub().callsFake((routes) => routes);
