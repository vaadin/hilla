/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.hilla.parser.models;

import io.github.classgraph.PackageInfo;

public abstract class PackageInfoModel extends AnnotatedAbstractModel
        implements Model, NamedModel {
    public static PackageInfoModel of(Package origin) {
        return new PackageInfoReflectionModel(origin);
    }

    @Deprecated
    public static PackageInfoModel of(PackageInfo origin) {
        return new PackageInfoSourceModel(origin);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PackageInfoModel)) {
            return false;
        }

        var other = (PackageInfoModel) obj;

        return getName().equals(other.getName());
    }

    @Override
    public Class<PackageInfoModel> getCommonModelClass() {
        return PackageInfoModel.class;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return "PackageInfoModel[" + get() + "]";
    }
}
