package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;

import org.reasm.Value;
import org.reasm.ValueVisitor;
import org.reasm.expressions.EvaluationContext;
import org.reasm.expressions.Expression;
import org.reasm.expressions.SymbolLookup;

final class ExpressionWithOverriddenSymbolLookup extends Expression {

    @Nonnull
    private final Expression wrappedExpression;
    @Nonnull
    private final SymbolLookup symbolLookup;

    ExpressionWithOverriddenSymbolLookup(@Nonnull Expression wrappedExpression, @Nonnull SymbolLookup symbolLookup) {
        this.wrappedExpression = wrappedExpression;
        this.symbolLookup = symbolLookup;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final ExpressionWithOverriddenSymbolLookup other = (ExpressionWithOverriddenSymbolLookup) obj;
        if (!this.wrappedExpression.equals(other.wrappedExpression)) {
            return false;
        }

        if (!this.symbolLookup.equals(other.symbolLookup)) {
            return false;
        }

        return true;
    }

    @Override
    public final Value evaluate(EvaluationContext evaluationContext) {
        return this.wrappedExpression.evaluate(this.hookedEvaluationContext(evaluationContext));
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.wrappedExpression.hashCode();
        result = prime * result + this.symbolLookup.hashCode();
        return result;
    }

    @Override
    public final String toIdentifier(EvaluationContext evaluationContext, ValueVisitor<String> valueVisitor) {
        return this.wrappedExpression.toIdentifier(this.hookedEvaluationContext(evaluationContext), valueVisitor);
    }

    private final EvaluationContext hookedEvaluationContext(EvaluationContext evaluationContext) {
        return new EvaluationContext(evaluationContext.getAssembly(), evaluationContext.getProgramCounter(), this.symbolLookup,
                evaluationContext.getAssemblyMessageConsumer());
    }

}
