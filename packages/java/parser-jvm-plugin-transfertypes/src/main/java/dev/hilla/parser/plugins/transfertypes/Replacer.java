package dev.hilla.parser.plugins.transfertypes;

import dev.hilla.parser.core.ClassMappers;

interface Replacer {
    void process();

    void setClassMappers(ClassMappers classMappers);
}
