package org.reasm.m68k.expressions.internal;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.expressions.BinaryOperator;

@Immutable
enum M68KBinaryOperator {

    /** The multiplication operator (infix *). */
    MULTIPLICATION(0, BinaryOperator.MULTIPLICATION),

    /** The division operator (infix /). */
    DIVISION(0, BinaryOperator.DIVISION),

    /** The modulus operator (infix %). */
    MODULUS(0, BinaryOperator.MODULUS),

    /** The addition operator (infix +). */
    ADDITION(1, BinaryOperator.ADDITION),

    /** The subtraction operator (infix -). */
    SUBTRACTION(1, BinaryOperator.SUBTRACTION),

    /** The bit shift left operator (infix <<). */
    BIT_SHIFT_LEFT(2, BinaryOperator.BIT_SHIFT_LEFT),

    /** The bit shift right operator (infix >>). */
    BIT_SHIFT_RIGHT(2, BinaryOperator.BIT_SHIFT_RIGHT),

    /** The less than operator (infix <). */
    LESS_THAN(3, BinaryOperator.LESS_THAN),

    /** The less than or equal to operator (infix <=). */
    LESS_THAN_OR_EQUAL_TO(3, BinaryOperator.LESS_THAN_OR_EQUAL_TO),

    /** The greater than operator (infix >). */
    GREATER_THAN(3, BinaryOperator.GREATER_THAN),

    /** The greater than or equal to operator (infix >=). */
    GREATER_THAN_OR_EQUAL_TO(3, BinaryOperator.GREATER_THAN_OR_EQUAL_TO),

    /** The equal to operator (infix =). */
    EQUAL_TO(4, BinaryOperator.EQUAL_TO),

    /** The different from operator (infix <>). */
    DIFFERENT_FROM(4, BinaryOperator.DIFFERENT_FROM),

    /** The strictly equal to operator (infix ==). */
    STRICTLY_EQUAL_TO(4, BinaryOperator.STRICTLY_EQUAL_TO),

    /** The strictly different from operator (infix !=). */
    STRICTLY_DIFFERENT_FROM(4, BinaryOperator.STRICTLY_DIFFERENT_FROM),

    /** The bitwise AND operator (infix &). */
    BITWISE_AND(5, BinaryOperator.BITWISE_AND),

    /** The bitwise XOR operator (infix ^). */
    BITWISE_XOR(6, BinaryOperator.BITWISE_XOR),

    /** The bitwise OR operator (infix |). */
    BITWISE_OR(7, BinaryOperator.BITWISE_OR),

    /** The logical AND operator (infix &&). */
    LOGICAL_AND(8, BinaryOperator.LOGICAL_AND),

    /** The logical OR operator (infix ||). */
    LOGICAL_OR(9, BinaryOperator.LOGICAL_OR);

    private final int priority;
    @Nonnull
    private final BinaryOperator operator;

    /**
     * Initializes a new BinaryOperator.
     *
     * @param priority
     *            the operator's priority (lower value means higher priority)
     */
    private M68KBinaryOperator(int priority, @Nonnull BinaryOperator operator) {
        this.priority = priority;
        this.operator = operator;
    }

    @Nonnull
    public final BinaryOperator getOperator() {
        return this.operator;
    }

    /**
     * Gets this operator's priority. A lower value means higher priority.
     *
     * @return the operator's priority
     */
    public final int getPriority() {
        return this.priority;
    }

}
