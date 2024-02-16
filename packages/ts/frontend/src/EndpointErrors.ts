/**
 * An exception that gets thrown when the Vaadin backend responds
 * with not ok status.
 */
export class EndpointError extends Error {
  /**
   * The optional detail object, containing additional information sent
   * from the backend
   */
  detail?: unknown;
  /**
   * The optional name of the exception that was thrown on a backend
   */
  type?: string;

  /**
   * @param message - the `message` property value
   * @param type - the `type` property value
   * @param detail - the `detail` property value
   */
  constructor(message: string, type?: string, detail?: unknown) {
    super(message);
    this.type = type;
    this.detail = detail;
  }
}

/**
 * An exception that gets thrown if Vaadin endpoint responds
 * with non-ok status and provides additional info
 * on the validation errors occurred.
 */
export class EndpointValidationError extends EndpointError {
  /**
   * An array of the validation errors.
   */
  validationErrorData: ValidationErrorData[];
  /**
   * An original validation error message.
   */
  validationErrorMessage: string;

  /**
   * @param message - the `message` property value
   * @param validationErrorData - the `validationErrorData` property value
   * @param type - the `type` property value
   */
  constructor(message: string, validationErrorData: ValidationErrorData[], type?: string) {
    super(message, type, validationErrorData);
    this.validationErrorMessage = message;
    this.detail = null;
    this.validationErrorData = validationErrorData;
  }
}

/**
 * An exception that gets thrown for unexpected HTTP response.
 */
export class EndpointResponseError extends EndpointError {
  /**
   * The optional response object, containing the HTTP response error
   */
  response: Response;

  /**
   * @param message - the `message` property value
   * @param response - the `response` property value
   */
  constructor(message: string, response: Response) {
    super(message, 'EndpointResponseError', response);
    this.response = response;
  }

  /**
   * Convenience property to get the HTTP code status directly
   */
  get status(): number {
    return this.response.status;
  }
}

export class UnauthorizedResponseError extends EndpointResponseError {
  constructor(message: string, response: Response) {
    super(message, response);
    this.type = 'UnauthorizedResponseError';
  }
}

export class ForbiddenResponseError extends EndpointResponseError {
  constructor(message: string, response: Response) {
    super(message, response);
    this.type = 'ForbiddenResponseError';
  }
}

/**
 * An object, containing all data for the particular validation error.
 */
export class ValidationErrorData {
  /**
   * The validation error message.
   */
  message: string;

  /**
   * The parameter name that caused the validation error.
   */
  parameterName?: string;

  /**
   * Validator original message
   */
  validatorMessage?: string;

  /**
   * @param message - The `message` property value
   * @param parameterName - The `parameterName` property value
   * @param validatorMessage - The `validatorMessage` property value
   */
  constructor(message: string, parameterName?: string, validatorMessage?: string) {
    this.message = message;
    this.parameterName = parameterName;
    this.validatorMessage = validatorMessage;
  }
}
