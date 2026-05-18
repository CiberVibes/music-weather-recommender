package es.ulpgc.dacd.lastfm.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TagTest {

    @Test
    void givenTagData_whenCreated_thenGettersReturnExpectedValues() {
        Tag tag = new Tag("pop", 50);

        assertEquals("pop", tag.getName());
        assertEquals(50, tag.getCount());
    }
}
