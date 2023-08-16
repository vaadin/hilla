const expect = require('chai').expect;
const creator = require('../src/creator.js');

describe('Package json creator', function () {
    it('should replace dependencies with a valid npmName of npmVersions', function () {
        const testVersions = {
            "foo-bar": {
                "npmName": "@foo/foo-bar",
                "npmVersion": "3.33",
                "javaVersion": "2.22",
                "jsVersion": "1.11"
            }
        };

        const testTemplate = {
            foo: "bar",
            dependencies: "removed"
        };

        const expectedResult = {
            foo: "bar",
            dependencies: {
                "@foo/foo-bar": "^3.33",
            }
        };

        const result = creator.createPackageJson(testVersions, testTemplate);

        expect(result).to.equal(JSON.stringify(expectedResult, null, 2));
    });

    it('should skip no npmName dependencies', function () {
        const testVersions = {
            "bar-foo": {
                "javaVersion": "2.22",
                "jsVersion": "3.33"
            }
        };

        const testTemplate = {
            foo: "bar",
            dependencies: "removed"
        };

        const expectedResult = {
            foo: "bar",
            dependencies: {}
        };

        const result = creator.createPackageJson(testVersions, testTemplate);

        expect(result).to.equal(JSON.stringify(expectedResult, null, 2));
    });

    it('should skip use jsVersion if npmVersion is not found', function () {
        const testVersions = {
            "bar-foo": {
                "npmName": "@foo/bar-foo",
                "javaVersion": "2.22",
                "jsVersion": "3.33"
            }
        };

        const testTemplate = {
            foo: "bar",
            dependencies: "removed"
        };

        const expectedResult = {
            foo: "bar",
            dependencies: {
                "@foo/bar-foo": "^3.33"
            }
        };

        const result = creator.createPackageJson(testVersions, testTemplate);

        expect(result).to.equal(JSON.stringify(expectedResult, null, 2));
    });
});

describe('Maven creator', function () {
    it('should replace dependencies with a valid XML of Java versions', function () {
        const testVersions = {
            "core": {
                "foo-bar": {
                    "javaVersion": "2.22",
                    "jsVersion": "1.11"
                }
            }
        };

        const testTemplate = "<dependencies>\n{{javadeps}}</dependencies>\n<version>${foo.bar.version}</version>";

        const expectedResult = "<dependencies>\n        <foo.bar.version>2.22</foo.bar.version>\n</dependencies>\n<version>${foo.bar.version}</version>";

        const result = creator.createMaven(testVersions, testTemplate);

        expect(result).to.equal(expectedResult);
    });

    it('should replace platform version with a version from input', function () {
        const testVersions = {
            "platform": "42.0.1"
        };

        const testTemplate = "<version>{{platform}}</version>";

        const expectedResult = "<version>42.0.1</version>";

        const result = creator.createMaven(testVersions, testTemplate);

        expect(result).to.equal(expectedResult);
    });

    it('should skip pure Javascript dependencies', function () {
        const testVersions = {
            "core": {
                "foo-bar": {
                    "javaVersion": "2.22",
                    "jsVersion": "1.11"
                },
                "bar-foo": {
                    "jsVersion": "2.22"
                }
            }
        };

        const testTemplate = "<dependencies>\n{{javadeps}}</dependencies>\n<version>${foo.bar.version}</version>";

        const expectedResult = "<dependencies>\n        <foo.bar.version>2.22</foo.bar.version>\n</dependencies>\n<version>${foo.bar.version}</version>";

        const result = creator.createMaven(testVersions, testTemplate);

        expect(result).to.equal(expectedResult);
    });

    it('should skip unused properties', function () {
        const testVersions = {
            "core": {
                "foo-bar": {
                    "javaVersion": "2.22",
                    "jsVersion": "1.11"
                },
                "bar-foo": {
                    "jsVersion": "2.22",
                    "javaVersion": "1.2.3"
                }
            }
        };

        const testTemplate = "<dependencies>\n{{javadeps}}</dependencies>\n<version>${bar.foo.version}</version>";

        const expectedResult = "<dependencies>\n        <bar.foo.version>1.2.3</bar.foo.version>\n</dependencies>\n<version>${bar.foo.version}</version>";

        const result = creator.createMaven(testVersions, testTemplate);

        expect(result).to.equal(expectedResult);
    });
});

describe('Release notes creator', function () {
    it('should add valid component descriptions for pro components', function() {
        const testVersions = {
            "platform": "11.0-SNAPSHOT",
            "core": {
                "foo-bar": {
                    "javaVersion": "2.22",
                    "jsVersion": "1.11",
                    "component": true,
                    "pro": true,
                }
            }
        };

        const testTemplate = "{{components}}";

        const expectedResult = '- Foo Bar **(PRO)** ([web component v1.11](https://github.com/vaadin/foo-bar/releases/tag/v1.11))\n';

        const result = creator.createReleaseNotes(testVersions, testTemplate);

        expect(result).to.equal(expectedResult);
    });

    it('should add valid component descriptions for core components', function() {
        const testVersions = {
            "platform": "11.0-SNAPSHOT",
            "core": {
                "foo-bar": {
                    "javaVersion": "2.22",
                    "jsVersion": "1.11",
                    "component": true
                }
            }
        };

        const testTemplate = "{{components}}";

        const expectedResult = '- Foo Bar ([web component v1.11](https://github.com/vaadin/foo-bar/releases/tag/v1.11))\n';

        const result = creator.createReleaseNotes(testVersions, testTemplate);

        expect(result).to.equal(expectedResult);
    });

    it('should add valid component descriptions for javascript components', function() {
        const testVersions = {
            "platform": "11.0-SNAPSHOT",
            "core": {
                "foo-bar": {
                    "jsVersion": "1.11",
                    "component": true
                }
            }
        };

        const testTemplate = "{{components}}";

        const expectedResult = '- Foo Bar ([web component v1.11](https://github.com/vaadin/foo-bar/releases/tag/v1.11))\n';

        const result = creator.createReleaseNotes(testVersions, testTemplate);

        expect(result).to.equal(expectedResult);
    });

    it('should skip non-components', function() {
        const testVersions = {
            "platform": "11.0-SNAPSHOT",
            "core": {
                "foo-bar": {
                    "javaVersion": "2.22",
                    "jsVersion": "1.11",
                }
            }
        };

        const testTemplate = "{{components}}";

        const expectedResult = '';

        const result = creator.createReleaseNotes(testVersions, testTemplate);

        expect(result).to.equal(expectedResult);
    });

    it('should generate changes string for changed dependencies', function() {
        const previousVersions = {
            "foo-bar": {
                "javaVersion": "2.22.2",
                "jsVersion": "1.11.1",
                "component": true
            },
            "bar-bar": {
                "jsVersion": "1.11.1",
            },
            "vaadin-designer": {
                "javaVersion": "4.0.0.alpha1"
            },
            "mpr-v7": {
                "javaVersion": "2.22.3"
            }
        };

        const currentVersions = {
            "foo-bar": {
                "javaVersion": "2.22.3",
                "jsVersion": "1.11.2",
                "component": true
            },
            "bar-bar": {
                "jsVersion": "1.11.1",
            },
            "vaadin-designer": {
                "javaVersion": "4.0.0.alpha2"
            },
            "mpr-v7": {
                "javaVersion": "2.22.4.alpha1"
            }
        };

        const expectedResult = '- Vaadin Designer **(Pro)** ([4.0.0.alpha2](https://github.com/vaadin/designer/releases/tag/4.0.0.alpha2))\n'
        + '- Vaadin Multiplatform Runtime **(Prime)** for Framework 7 ([2.22.4.alpha1](https://github.com/vaadin/multiplatform-runtime/releases/tag/2.22.4.alpha1))\n'
        + '- Foo Bar ([web component v1.11.2](https://github.com/vaadin/foo-bar/releases/tag/v1.11.2))\n';

        const result = creator.generateChangesString(currentVersions, previousVersions);

        expect(result).to.equal(expectedResult);
    });

    it('should calculate previous versions correctly', function() {
        const maintenanceVersion = '1.2.3';
        const expectedPreviousMaintenanceVersion = '1.2.2';
        const previousMaintenanceVersion = creator.calculatePreviousVersion(maintenanceVersion);
        expect(previousMaintenanceVersion).to.equal(expectedPreviousMaintenanceVersion);

        const alphaVersion = '1.2.0.alpha2';
        const expectedAlphaVersion = '1.2.0.alpha1';
        const previousAlphaVersion = creator.calculatePreviousVersion(alphaVersion);
        expect(previousAlphaVersion).to.equal(expectedAlphaVersion);

        const snapshotVersion = '12.0-SNAPSHOT';
        const previousSnapshotVersion = creator.calculatePreviousVersion(snapshotVersion);
        expect(previousSnapshotVersion).to.equal('');
    });
});
