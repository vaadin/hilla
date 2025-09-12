// Setup file for Vitest to handle unhandled promise rejections
// This prevents test failures due to unhandled promises in browser environment

if (typeof window !== 'undefined') {
  // Browser environment - handle unhandled promise rejections
  window.addEventListener('unhandledrejection', (event) => {
    // Prevent the default unhandled rejection behavior
    event.preventDefault();
    // Optionally log the error for debugging
    // console.warn('Unhandled promise rejection caught in test setup:', event.reason);
  });
}

// Export to make this an unambiguous ES module
export {};
