import sinon from 'sinon';

export const protectRoute = sinon.stub().callsFake((route) => route);
