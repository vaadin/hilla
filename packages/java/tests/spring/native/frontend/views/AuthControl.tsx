import { AccessProps, AuthContext } from 'Frontend/useAuth.js';
import { ReactNode, useContext } from 'react';
import { Navigate, useMatches } from 'react-router-dom';

export type AuthControlProps = Readonly<{
  fallback?: ReactNode;
  children?: ReactNode;
}>;

export default function AuthControl({ fallback, children }: AuthControlProps) {
  const { state, hasAccess } = useContext(AuthContext);
  const matches = useMatches();

  if (fallback && (state.initializing || state.loading)) {
    return <>{fallback}</>;
  }

  const authorized = matches.every((match) => hasAccess(match.handle as AccessProps));
  if (!authorized) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}
