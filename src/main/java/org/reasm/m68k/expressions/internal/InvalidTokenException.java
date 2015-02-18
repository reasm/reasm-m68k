package org.reasm.m68k.expressions.internal;

import javax.annotation.Nonnull;

import org.reasm.commons.messages.InvalidTokenErrorMessage;

/**
 * A checked exception that is thrown when the expression parser encounters an invalid token.
 *
 * @author Francis Gagné
 */
public final class InvalidTokenException extends Exception {

    private static final long serialVersionUID = 1L;

    @Nonnull
    private final String token;

    /**
     * Initializes a new InvalidTokenException.
     *
     * @param token
     *            the invalid token
     */
    public InvalidTokenException(@Nonnull String token) {
        if (token == null) {
            throw new NullPointerException("token");
        }

        this.token = token;
    }

    /**
     * Creates an {@link InvalidTokenErrorMessage} with the token that triggered this exception.
     *
     * @return a new {@link InvalidTokenErrorMessage}
     */
    @Nonnull
    public final InvalidTokenErrorMessage createAssemblyErrorMessage() {
        return new InvalidTokenErrorMessage(this.token);
    }

    /**
     * Gets the invalid token that triggered this exception.
     *
     * @return the invalid token
     */
    @Nonnull
    public final String getToken() {
        return this.token;
    }

}
