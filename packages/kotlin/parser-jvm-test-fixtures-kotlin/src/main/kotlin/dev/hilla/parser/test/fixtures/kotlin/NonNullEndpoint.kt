package dev.hilla.parser.test.fixtures.kotlin

import dev.hilla.Endpoint
import dev.hilla.Nonnull
import java.math.BigDecimal

@Endpoint
class NonNullEndpoint {
  fun getString(): String {
    return "";
  }

  fun saveString(string: String) {
  }

  fun getLong(): Long {
    return 0L;
  }

  fun saveLong(long: Long) {
  }

  fun getBigDecimal(): BigDecimal {
    return BigDecimal.ZERO;
  }

  fun saveBigDecimal(bigDecimal: BigDecimal) {
  }

  fun getStringList(): List<String> {
    return listOf();
  }

  fun saveStringList(list: List<String>) {
  }

  fun getBigDecimalList(): List<BigDecimal> {
    return listOf();
  }

  fun saveBigDecimalList(list: List<BigDecimal>) {
  }

  fun getNonnullLongList(): List<@Nonnull Long> {
    return listOf();
  }

  fun saveNonnullLongList(list: List<@Nonnull Long>) {
  }

  fun getNonnullStringList(): List<@Nonnull String> {
    return listOf();
  }

  fun saveNonnullStringList(list: List<@Nonnull String>) {
  }

  fun getNonnullBigDecimalList(): List<@Nonnull BigDecimal> {
    return listOf();
  }

  fun saveNonnullBigDecimalList(list: List<@Nonnull BigDecimal>) {
  }

  fun getLongList(): List<Long> {
    return listOf();
  }

  fun saveLongList(list: List<Long>) {
  }

  fun getStringMap(): Map<String, String> {
    return mapOf();
  }

  fun saveStringMap(map: Map<String, String>) {
  }

  fun getComplexType(): Map<String, List<String>> {
    return mapOf();
  }

  fun saveComplexType(map: Map<String, List<String>>) {
  }
}
