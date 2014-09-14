package org.reasm.m68k.messages;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.reasm.AssemblyErrorMessage;
import org.reasm.m68k.Expressions;

/**
 * An error message that is generated during an assembly when a string is too long in a particular context.
 *
 * @author Francis Gagné
 */
public class StringTooLongErrorMessage extends AssemblyErrorMessage {

    @Nonnull
    private final String string;

    /**
     * Initializes a new StringTooLongErrorMessage.
     *
     * @param string
     *            the invalid string
     */
    public StringTooLongErrorMessage(@Nonnull String string) {
        super("String value too long: " + Expressions.serializeString(Objects.requireNonNull(string, "string")));
        this.string = string;
    }

    /**
     * Gets the string that generated this error message.
     *
     * @return the string
     */
    @Nonnull
    public final String getString() {
        return this.string;
    }

}
