package com.manchesterbaby.baby.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins the store's string formatting, which the snapshot and assembly file formats and the
 * disassembler all depend on. The NUM operand format follows a user preference, so it is
 * left alone here.
 */
class StoreStringsTest {

    @Test
    void binaryStringIsAlways32BitsMsbFirst() {
        assertEquals("00000000000000000000000000000000", Store.getBinaryString(0));
        assertEquals("00000000000000000000000000000001", Store.getBinaryString(1));
        assertEquals("11111111111111111111111111010110", Store.getBinaryString(-42));
        // the top bit set is the case where toBinaryString needs no padding at all
        assertEquals("10000000000000000000000000000000", Store.getBinaryString(Integer.MIN_VALUE));
    }

    @Test
    void disassemblyIsPaddedToTheCommentColumn() {
        Store store = new Store();
        store.setLine(5, 99);

        // mnemonic, padded to 20 chars, then hex, decimal, and the operand line's value
        assertEquals("LDN 5               ;  00004005 : 16389 (99)", store.disassembleModern(0x4005));
        // isNextInstruction marks the comment instead
        assertEquals("LDN 5               ;* 00004005 : 16389 (99)", store.disassembleModern(0x4005, true));
        // a value with bits outside 0-4 and 13-15 set, disassembled with NUM turned off
        assertEquals("JRP 25              ;  00003039 : 12345 (0)", store.disassembleModern(12345, false, false));
    }
}
