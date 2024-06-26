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
package com.arcadedb.query.sql.method.misc;

import com.arcadedb.database.Identifiable;
import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.method.AbstractSQLMethod;
import com.arcadedb.utility.DateUtils;

import java.time.*;
import java.time.temporal.*;
import java.util.*;

/**
 * Modifies the precision of a datetime.
 *
 * @author Luca Garulli (l.garulli--(at)--gmail.com)
 */
public class SQLMethodPrecision extends AbstractSQLMethod {

  public static final String NAME = "precision";

  public SQLMethodPrecision() {
    super(NAME, 1, 1);
  }

  @Override
  public Object execute(final Object value, final Identifiable iCurrentRecord, final CommandContext iContext, final Object[] iParams) {
    if (iParams == null || iParams.length == 0 || iParams[0] == null)
      throw new IllegalArgumentException("precision method was expecting the time unit");

    final ChronoUnit targetPrecision = DateUtils.parsePrecision(iParams[0].toString());

    if (value instanceof LocalDateTime)
      return ((LocalDateTime) value).truncatedTo(targetPrecision);
    else if (value instanceof ZonedDateTime)
      return ((ZonedDateTime) value).truncatedTo(targetPrecision);
    else if (value instanceof Instant)
      return ((Instant) value).truncatedTo(targetPrecision);
    else if (value instanceof Date) {
      if (targetPrecision == ChronoUnit.MILLIS)
        return value;
      return DateUtils.dateTime(iContext.getDatabase(), ((Date) value).getTime(), ChronoUnit.MILLIS, LocalDateTime.class, targetPrecision);
    } else if (value instanceof Calendar) {
      if (targetPrecision == ChronoUnit.MILLIS)
        return value;
      return DateUtils.dateTime(iContext.getDatabase(), ((Calendar) value).getTimeInMillis(), ChronoUnit.MILLIS, LocalDateTime.class, targetPrecision);
    }

    throw new CommandExecutionException("Error on changing precision for unsupported type '" + value.getClass() + "'");
  }
}
