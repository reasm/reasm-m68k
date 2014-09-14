package org.reasm.m68k.messages;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an instruction or directive specifies an invalid size attribute.
 *
 * @author Francis Gagné
 */
public class InvalidSizeAttributeErrorMessage extends AssemblyErrorMessage {

    @Nonnull
    private final String size;

    /**
     * Initializes a new InvalidSizeAttributeErrorMessage.
     *
     * @param size
     *            the size attribute
     */
    public InvalidSizeAttributeErrorMessage(@Nonnull String size) {
        super("Invalid size attribute: " + Objects.requireNonNull(size, "size"));
        this.size = size;
    }

    /**
     * Gets the invalid size attribute that generated this error message.
     *
     * @return the invalid size attribute
     */
    @Nonnull
    public final String getSize() {
        return this.size;
    }

}
