package dev.hilla.parser.plugins.transfertypes;

import dev.hilla.parser.core.ClassMappers;

interface Replacer {
    void setClassMappers(ClassMappers classMappers);

    void process();
}
