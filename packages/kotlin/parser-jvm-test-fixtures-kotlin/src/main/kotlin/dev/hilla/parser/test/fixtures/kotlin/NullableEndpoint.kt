package dev.hilla.parser.test.fixtures.kotlin

import dev.hilla.Endpoint
import dev.hilla.Nonnull
import java.math.BigDecimal

@Endpoint
class NullableEndpoint {
  fun getString(): String? {
    return null;
  }

  fun saveString(string: String?) {
  }

  fun getLong(): Long? {
    return null;
  }

  fun saveLong(long: Long?) {
  }

  fun getBigDecimal(): BigDecimal? {
    return BigDecimal.ZERO;
  }

  fun saveBigDecimal(bigDecimal: BigDecimal?) {
  }

  fun getStringList(): List<String?>? {
    return null;
  }

  fun saveStringList(list: List<String?>?) {
  }

  fun getBigDecimalList(): List<BigDecimal?>? {
    return null;
  }

  fun saveBigDecimalList(list: List<BigDecimal?>?) {
  }

  fun getNonnullLongList(): List<@Nonnull Long?>? {
    return null;
  }

  fun saveNonnullLongList(list: List<@Nonnull Long?>?) {
  }

  fun getNonnullStringList(): List<@Nonnull String?>? {
    return null;
  }

  fun saveNonnullStringList(list: List<@Nonnull String?>?) {
  }

  fun getNonnullBigDecimalList(): List<@Nonnull BigDecimal?>? {
    return null;
  }

  fun saveNonnullBigDecimalList(list: List<@Nonnull BigDecimal?>?) {
  }

  fun getLongList(): List<Long?>? {
    return null;
  }

  fun saveLongList(list: List<Long?>?) {
  }

  fun getStringMap(): Map<String, String?>? {
    return null;
  }

  fun saveStringMap(map: Map<String, String?>?) {
  }

  fun getComplexType(): Map<String, List<String?>?>? {
    return null;
  }

  fun saveComplexType(map: Map<String, List<String?>?>?) {
  }
}
