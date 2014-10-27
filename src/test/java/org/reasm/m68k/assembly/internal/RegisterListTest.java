package org.reasm.m68k.assembly.internal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.EnumSet;
import java.util.Set;

import org.junit.Test;

import ca.fragag.testhelpers.ObjectHashCodeEqualsContract;

/**
 * Test class for {@link RegisterList}.
 *
 * @author Francis Gagné
 */
public class RegisterListTest extends ObjectHashCodeEqualsContract {

    /**
     * Initializes a new RegisterListTest.
     */
    public RegisterListTest() {
        super(new RegisterList(EnumSet.of(GeneralPurposeRegister.D0, GeneralPurposeRegister.D3, GeneralPurposeRegister.A1)),
                new RegisterList(EnumSet.of(GeneralPurposeRegister.D0, GeneralPurposeRegister.D3, GeneralPurposeRegister.A1)),
                new RegisterList(EnumSet.of(GeneralPurposeRegister.D0, GeneralPurposeRegister.D3, GeneralPurposeRegister.A1)),
                new RegisterList(EnumSet.of(GeneralPurposeRegister.D0, GeneralPurposeRegister.D3)), new Object());
    }

    /**
     * Asserts that {@link RegisterList#RegisterList(Set)} initializes a {@link RegisterList} instance correctly.
     */
    @Test
    public void registerList() {
        final Set<GeneralPurposeRegister> set = EnumSet.of(GeneralPurposeRegister.D0, GeneralPurposeRegister.D3,
                GeneralPurposeRegister.A1);
        final RegisterList registerList = new RegisterList(set);
        assertThat(registerList.getRegisters(), is(sameInstance(set)));
    }

}
