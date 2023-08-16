const path = require('path');
const fs = require('fs');
const argv = require('minimist')(process.argv.slice(2));

const writer = require('./src/writer');
const transformer = require('./src/transformer');

if (!argv['platform']) {
    console.log('Specify platform version as \'--platform=11.12.13\'');
    process.exit(1);
}

if (!argv['versions']) {
    console.log('Specify product version file as \'--versions=versions.json\'');
    process.exit(1);
}

const versionsFileName = path.resolve(argv['versions']);
const resultsDir = path.resolve(`${__dirname}/results`);

const inputVersions = require(versionsFileName);

function getTemplateFilePath(filename) {
    return path.resolve(`${__dirname}/templates/${filename}`);
}

function getResultsFilePath(filename) {
    return path.resolve(`${__dirname}/results/${filename}`);
}

const hillaJsonTemplateFileName = getTemplateFilePath('template-hilla-versions.json');
const hillaJsonResultFileName = getResultsFilePath('hilla-versions.json');

const hillaReactJsonTemplateFileName = getTemplateFilePath('template-hilla-react-versions.json');
const hillaReactJsonResultFileName = getResultsFilePath('hilla-react-versions.json');

const platform=argv['platform'];
const versions = transformer.transformVersions(inputVersions, platform, argv['useSnapshots']);

if (!fs.existsSync(resultsDir)) {
    fs.mkdirSync(resultsDir);
}

writer.writeSeparateJson(versions.bundles, hillaJsonTemplateFileName, hillaJsonResultFileName, "bundles");
writer.writeSeparateJson(versions.core, hillaJsonTemplateFileName, hillaJsonResultFileName, "core");
writer.writeSeparateJson(versions.vaadin, hillaJsonTemplateFileName, hillaJsonResultFileName, "vaadin");
writer.writeSeparateJson(versions.bundles, hillaReactJsonTemplateFileName, hillaReactJsonResultFileName, "bundles");
writer.writeSeparateJson(versions.react, hillaReactJsonTemplateFileName, hillaReactJsonResultFileName, "react");

const hilla = process.env.HILLA || platform.replace(/^24/, 2);
versions.core.hilla = {javaVersion: hilla};
// write hilla version to hilla-react-versions.json as platform
const hillaVersions = {platform : hilla};
writer.writeSeparateJson(hillaVersions.platform, hillaJsonTemplateFileName, hillaJsonResultFileName, "platform");
writer.writeSeparateJson(hillaVersions.platform, hillaReactJsonTemplateFileName, hillaReactJsonResultFileName, "platform");
