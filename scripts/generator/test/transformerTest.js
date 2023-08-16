const expect = require('chai').expect;
const transformer = require('../src/transformer.js');

describe('Version transformer', function () {
    it('should set the given platform version', function() {
        const testVersions = {
            "foo-bar": {
                "javaVersion": "2.22",
                "jsVersion": "1.11"
            },

            "lorem": "{{version}}"
        };

        const result = transformer.transformVersions(testVersions, "1.2.3", false);

        expect(result.lorem).to.equal("1.2.3");
    });

    it('should transform java versions to snapshots', function() {
        const testVersions = {
            "foo-bar": {
                "javaVersion": "2.22",
                "jsVersion": "1.11"
            },
            "bar-foo": {
                "javaVersion": "4.3.beta2",
                "jsVersion": "5.7.beta33"
            },

            "lorem": "{{version}}"
        };

        const result = transformer.transformVersions(testVersions, "1.2.3", true);

        expect(result['foo-bar'].javaVersion).to.equal("2.22-SNAPSHOT");
        expect(result['foo-bar'].jsVersion).to.equal("1.11");
        expect(result['bar-foo'].javaVersion).to.equal("4.3-SNAPSHOT");
        expect(result['bar-foo'].jsVersion).to.equal("5.7.beta33");

        expect(result.lorem).to.equal("1.2.3");
    });
});