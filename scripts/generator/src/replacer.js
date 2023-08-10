/**
Originally from https://github.com/lightsofapollo/json-templater

Modified regex.

@param {String} path dotted to indicate levels in an object.
@param {Object} view for the data.
*/
function extractValue(path, view) {
  // Short circuit for direct matches.
  if (view && view[path]) return view[path];

  var parts = path.split('.');

  while (
    // view should always be truthy as all objects are.
    view &&
    // must have a part in the dotted path
    (part = parts.shift())
  ) {
    view = (typeof view === 'object' && part in view) ?
      view[part] :
      undefined;
  }

  return view;
}

var REGEX = new RegExp('{{(.*?)}}', 'g');
var TEMPLATE_OPEN = '{{';

/**
@param {String} input template.
@param {Object} view details.
*/
function replace(input, view) {
  // optimization to avoid regex calls (indexOf is strictly faster)
  if (input.indexOf(TEMPLATE_OPEN) === -1) return input;
  var result;
  var replaced = input.replace(REGEX, function(original, path) {
    var value = extractValue(path, view);
    if (undefined === value || null === value) {
      return original;
    }

    if (typeof value === 'object') {
      result = value;
      return;
    }

    return value;
  });
  return (undefined !== result) ? result : replaced;
}

module.exports = replace;
