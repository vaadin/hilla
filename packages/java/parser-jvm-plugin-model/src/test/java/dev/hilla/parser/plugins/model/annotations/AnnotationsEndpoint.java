package dev.hilla.parser.plugins.model.annotations;

import dev.hilla.parser.plugins.model.Endpoint;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Version;

@Endpoint
public class AnnotationsEndpoint {
    public AnnotationTestEntity getTestEntity() {
        return new AnnotationTestEntity();
    }

    public static class AnnotationTestEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idgenerator")
        @SequenceGenerator(name = "idgenerator", initialValue = 1000)
        public Long id;

        private int version;

        @Column(name = "test_column")
        public String name;

        @Version
        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }
    }
}
