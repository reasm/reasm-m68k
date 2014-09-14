package org.reasm.m68k.assembly.internal;

import org.reasm.AssemblyStepIterationController;
import org.reasm.expressions.Expression;

final class WhileBlockState implements AssemblyStepIterationController {

    DynamicSourceLocationIterator iterator;
    boolean hasNextIteration = true;
    boolean parsedCondition;
    Expression conditionExpression;

    @Override
    public boolean hasNextIteration() {
        return this.hasNextIteration;
    }

}
