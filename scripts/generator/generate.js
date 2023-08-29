const path = require('path');
const fs = require('fs');

const writer = require('./src/writer');
const transformer = require('./src/transformer');

const hillaVersion = process.argv[2];
const versionsFileName = process.argv[3];
const resultsDir = path.resolve(`${__dirname}/results`);

const inputVersions = JSON.parse(fs.readFileSync(versionsFileName));

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

const versions = transformer.transformVersions(inputVersions, hillaVersion, false);
versions.platform = hillaVersion;

if (!fs.existsSync(resultsDir)) {
    fs.mkdirSync(resultsDir);
}

const releaseNotesTemplateFileName = getTemplateFilePath('template-release-note.md');
const releaseNotesResultFileName = getResultsFilePath('release-note.md');
const releaseNotesMaintenanceTemplateFileName = getTemplateFilePath('template-release-note-maintenance.md');
const releaseNotesMaintenanceResultFileName = getResultsFilePath('release-note-maintenance.md');
const releaseNotesPrereleaseTemplateFileName = getTemplateFilePath('template-release-note-prerelease.md');
const releaseNotesPrereleaseResultFileName = getResultsFilePath('release-note-prerelease.md');

writer.writeSeparateJson(versions.bundles, hillaJsonTemplateFileName, hillaJsonResultFileName, "bundles");
writer.writeSeparateJson(versions.core, hillaJsonTemplateFileName, hillaJsonResultFileName, "core");
writer.writeSeparateJson(versions.vaadin, hillaJsonTemplateFileName, hillaJsonResultFileName, "vaadin");
writer.writeSeparateJson(versions.bundles, hillaReactJsonTemplateFileName, hillaReactJsonResultFileName, "bundles");
writer.writeSeparateJson(versions.react, hillaReactJsonTemplateFileName, hillaReactJsonResultFileName, "react");

writer.writeReleaseNotes(versions, releaseNotesTemplateFileName, releaseNotesResultFileName);
writer.writeReleaseNotes(versions, releaseNotesMaintenanceTemplateFileName, releaseNotesMaintenanceResultFileName);
writer.writeReleaseNotes(versions, releaseNotesPrereleaseTemplateFileName, releaseNotesPrereleaseResultFileName);

versions.core.hilla = {javaVersion: hillaVersion};
// write hilla version to hilla-react-versions.json as platform
const hillaVersions = {platform : hillaVersion};
writer.writeSeparateJson(hillaVersions.platform, hillaJsonTemplateFileName, hillaJsonResultFileName, "platform");
writer.writeSeparateJson(hillaVersions.platform, hillaReactJsonTemplateFileName, hillaReactJsonResultFileName, "platform");
