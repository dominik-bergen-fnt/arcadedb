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
package com.arcadedb.query.sql.function.math;

import com.arcadedb.database.Identifiable;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.MultiValue;
import com.arcadedb.schema.Type;

/**
 * Computes the sum of field. Uses the context to save the last sum number. When different Number class are used, take the class
 * with most precision.
 *
 * @author Luca Garulli (l.garulli--(at)--gmail.com)
 */
public class SQLFunctionSum extends SQLFunctionMathAbstract {
  public static final String NAME = "sum";

  private Number sum;

  public SQLFunctionSum() {
    super(NAME);
  }

  public Object execute(final Object self, final Identifiable currentRecord, final Object currentResult, final Object[] params,
      final CommandContext context) {
    if (params.length == 1) {
      if (params[0] instanceof Number number)
        sum(number);
      else if (MultiValue.isMultiValue(params[0]))
        for (final Object n : MultiValue.getMultiValueIterable(params[0]))
          sum((Number) n);
    } else {
      sum = null;
      for (int i = 0; i < params.length; ++i)
        sum((Number) params[i]);
    }
    return sum;
  }

  protected void sum(final Number value) {
    if (value != null) {
      if (sum == null)
        // FIRST TIME
        sum = value;
      else
        sum = Type.increment(sum, value);
    }
  }

  @Override
  public boolean aggregateResults() {
    return configuredParameters.length == 1;
  }

  public String getSyntax() {
    return "sum(<field> [,<field>*])";
  }

  @Override
  public Object getResult() {
    return sum == null ? 0 : sum;
  }
}
