/**
@param {Object} versions data object for product versions.
@param {String} platformVersion template data object to put versions to.
@param {Boolean} useSnapshots transform all Java versions to snapshots
*/
function transformVersions(versions, platformVersion, useSnapshots) {
    if (!useSnapshots) {
        return transformPlatformVersion(versions, platformVersion);
    }

    const withPlatformVersion = transformPlatformVersion(versions, platformVersion);
    const withSnapshots = transformJavaSnapshots(withPlatformVersion);

    return withSnapshots;
}

function transformPlatformVersion(versions, platformVersion) {
    const platformVersionVisitor = (key, value, parent) => {
        if (value === '{{version}}') {
            parent[key] = platformVersion;
        }
    };
    const transformedVersions = Object.assign({}, versions);
    visit(transformedVersions, platformVersionVisitor);
    return transformedVersions;
}

function transformJavaSnapshots(versions) {
    const majorMinorVersions = /(\d*?\.\d*(?:\.[a-z]+)?).*/;
    const snapshotVersionVisitor = (key, value, parent) => {
        if (key === 'javaVersion') {
            parent[key] = value.replace(majorMinorVersions, "$1-SNAPSHOT");
        }
    };
    const transformedVersions = Object.assign({}, versions);
    visit(transformedVersions, snapshotVersionVisitor);
    return transformedVersions;
}

function visit(object, visitor, parent) {
    for (let [key, value] of Object.entries(object)) {
        visitor.apply(this, [key, value, object]);
        if (value instanceof Object && !(value instanceof Array)) {
            visit(value, visitor, object);
        }
    }
}

exports.transformVersions = transformVersions;
