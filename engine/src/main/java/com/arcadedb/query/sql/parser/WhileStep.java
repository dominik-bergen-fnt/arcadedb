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
package com.arcadedb.query.sql.parser;

import com.arcadedb.exception.TimeoutException;
import com.arcadedb.query.sql.executor.AbstractExecutionStep;
import com.arcadedb.query.sql.executor.BasicCommandContext;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.query.sql.executor.EmptyStep;
import com.arcadedb.query.sql.executor.ExecutionStepInternal;
import com.arcadedb.query.sql.executor.InternalExecutionPlan;
import com.arcadedb.query.sql.executor.ResultInternal;
import com.arcadedb.query.sql.executor.ResultSet;
import com.arcadedb.query.sql.executor.ScriptExecutionPlan;

import java.util.*;

public class WhileStep extends AbstractExecutionStep {
  private final BooleanExpression     condition;
  private final List<Statement>       statements;
  private       ExecutionStepInternal finalResult = null;

  public WhileStep(final BooleanExpression condition, final List<Statement> statements, final CommandContext context) {
    super(context);
    this.condition = condition;
    this.statements = statements;
  }

  @Override
  public ResultSet syncPull(final CommandContext context, final int nRecords) throws TimeoutException {
    if (prev != null)
      prev.syncPull(context, nRecords);

    if (finalResult != null)
      return finalResult.syncPull(context, nRecords);

    while (condition.evaluate(new ResultInternal(context.getDatabase()), context)) {
      final ScriptExecutionPlan plan = initPlan(context);
      final ExecutionStepInternal result = plan.executeFull();
      if (result != null) {
        this.finalResult = result;
        return result.syncPull(context, nRecords);
      }
    }
    finalResult = new EmptyStep(context);
    return finalResult.syncPull(context, nRecords);
  }

  public ScriptExecutionPlan initPlan(final CommandContext context) {
    final BasicCommandContext subCtx1 = new BasicCommandContext();
    subCtx1.setParent(context);
    final ScriptExecutionPlan plan = new ScriptExecutionPlan(subCtx1);
    for (final Statement stm : statements) {
      if (stm.originalStatement == null)
        stm.originalStatement = stm;

      InternalExecutionPlan subPlan;
      if (stm.getOriginalStatement().contains("?"))
        // cannot cache execution plans with positional parameters inside scripts
        subPlan = stm.createExecutionPlanNoCache(subCtx1);
      else
        subPlan = stm.createExecutionPlan(subCtx1);

      plan.chain(subPlan);
    }
    return plan;
  }

  public boolean containsReturn() {
    for (final Statement stm : this.statements) {
      if (stm instanceof ReturnStatement)
        return true;

      if (stm instanceof ForEachBlock block && block.containsReturn())
        return true;

      if (stm instanceof IfStatement statement && statement.containsReturn())
        return true;

      if (stm instanceof WhileBlock block && block.containsReturn())
        return true;
    }
    return false;
  }
}
