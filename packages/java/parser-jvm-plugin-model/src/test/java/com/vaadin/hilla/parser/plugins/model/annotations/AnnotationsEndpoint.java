package com.vaadin.hilla.parser.plugins.model.annotations;

import com.vaadin.hilla.parser.plugins.model.Endpoint;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Version;

import java.util.List;

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

        @OneToOne
        public NestedEntity oneToOne;

        @OneToMany
        public List<NestedEntity> oneToMany;

        @ManyToOne
        public NestedEntity manyToOne;

        @ManyToMany
        public List<NestedEntity> manyToMany;

        @ManyToMany(fetch = FetchType.EAGER)
        public List<NestedEntity> manyToManyWithFetchType;

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

    public static class NestedEntity {
        public String name;
    }
}
