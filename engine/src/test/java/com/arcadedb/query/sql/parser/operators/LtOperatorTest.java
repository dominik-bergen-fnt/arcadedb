/*
 * Copyright © 2021-present Arcade Data Ltd (info@arcadedata.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-FileCopyrightText: 2021-present Arcade Data Ltd (info@arcadedata.com)
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arcadedb.query.sql.parser.operators;

import com.arcadedb.query.sql.parser.LtOperator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author Luigi Dell'Aquila (luigi.dellaquila-(at)-gmail.com)
 */
public class LtOperatorTest {
  @Test
  public void test() {
    final LtOperator op = new LtOperator(-1);
    assertThat(op.execute(null, 1, 1)).isFalse();
    assertThat(op.execute(null, 1, 0)).isFalse();
    assertThat(op.execute(null, 0, 1)).isTrue();

    assertThat(op.execute(null, "aaa", "zzz")).isTrue();
    assertThat(op.execute(null, "zzz", "aaa")).isFalse();

    assertThat(op.execute(null, "aaa", "aaa")).isFalse();

    assertThat(op.execute(null, 1, 1.1)).isTrue();
    assertThat(op.execute(null, 1.1, 1)).isFalse();

    assertThat(op.execute(null, BigDecimal.ONE, 1)).isFalse();
    assertThat(op.execute(null, 1, BigDecimal.ONE)).isFalse();

    assertThat(op.execute(null, 1.1, 1.1)).isFalse();
    assertThat(op.execute(null, new BigDecimal(15), new BigDecimal(15))).isFalse();

    assertThat(op.execute(null, 1.1, BigDecimal.ONE)).isFalse();
    assertThat(op.execute(null, 2, BigDecimal.ONE)).isFalse();

    assertThat(op.execute(null, BigDecimal.ONE, 0.999999)).isFalse();
    assertThat(op.execute(null, BigDecimal.ONE, 0)).isFalse();

    assertThat(op.execute(null, BigDecimal.ONE, 2)).isTrue();
    assertThat(op.execute(null, BigDecimal.ONE, 1.0001)).isTrue();
    try {
      assertThat(op.execute(null, new Object(), new Object())).isTrue();
      fail("");
    } catch (final Exception e) {
      assertThat(e instanceof ClassCastException).isTrue();
    }

    // MAPS
    assertThat(op.execute(null, Map.of("a", "b"), Map.of("a", "b"))).isFalse();

    assertThat(op.execute(null, Map.of("a", "b", "c", 3), Map.of("a", "b", "c", 3))).isFalse();
    assertThat(op.execute(null, Map.of("a", "b", "c", 3), Map.of("a", "b"))).isFalse();

    assertThat(op.execute(null, Map.of("a", "b"), Map.of("a", "b", "c", 3))).isTrue();
  }
}
