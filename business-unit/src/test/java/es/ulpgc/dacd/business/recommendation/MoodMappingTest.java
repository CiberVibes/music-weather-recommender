package es.ulpgc.dacd.business.recommendation;

import es.ulpgc.dacd.business.model.MoodMapping;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoodMappingTest {

    @Test
    void givenClearWeather_whenGetMoodName_thenReturnsHappy() {
        String result = MoodMapping.moodName("Clear");
        assertEquals("Happy (Q1)", result);
    }

    @Test
    void givenThunderstormWeather_whenGetMoodName_thenReturnsAngry() {
        String result = MoodMapping.moodName("Thunderstorm");
        assertEquals("Angry (Q2)", result);
    }

    @Test
    void givenRainWeather_whenGetMoodName_thenReturnsSad() {
        String result = MoodMapping.moodName("Rain");
        assertEquals("Sad (Q3)", result);
    }

    @Test
    void givenDrizzleWeather_whenGetMoodName_thenReturnsSad() {
        String result = MoodMapping.moodName("Drizzle");
        assertEquals("Sad (Q3)", result);
    }

    @Test
    void givenSnowWeather_whenGetMoodName_thenReturnsSad() {
        String result = MoodMapping.moodName("Snow");
        assertEquals("Sad (Q3)", result);
    }

    @Test
    void givenFogWeather_whenGetMoodName_thenReturnsSad() {
        String result = MoodMapping.moodName("Fog");
        assertEquals("Sad (Q3)", result);
    }

    @Test
    void givenCloudsWeather_whenGetMoodName_thenReturnsRelaxed() {
        String result = MoodMapping.moodName("Clouds");
        assertEquals("Relaxed (Q4)", result);
    }

    @Test
    void givenUnknownWeather_whenGetMoodName_thenReturnsDefault() {
        String result = MoodMapping.moodName("Tornado");
        assertEquals("Happy (Q1)", result);
    }

    @Test
    void givenLowerCaseWeather_whenGetMoodName_thenReturnsCorrectMood() {
        assertEquals("Happy (Q1)", MoodMapping.moodName("clear"));
        assertEquals("Angry (Q2)", MoodMapping.moodName("thunderstorm"));
        assertEquals("Sad (Q3)", MoodMapping.moodName("rain"));
        assertEquals("Relaxed (Q4)", MoodMapping.moodName("clouds"));
    }

    @Test
    void givenClearWeather_whenGetTags_thenContainsMoodTagsFromResearch() {
        List<String> tags = MoodMapping.tagsFor("Clear");
        assertTrue(tags.contains("happy"));
        assertTrue(tags.contains("cheerful"));
        assertTrue(tags.contains("fun"));
    }

    @Test
    void givenClearWeather_whenGetTags_thenContainsGenreFallbacks() {
        List<String> tags = MoodMapping.tagsFor("Clear");
        assertTrue(tags.contains("pop"));
        assertTrue(tags.contains("dance"));
    }

    @Test
    void givenThunderstormWeather_whenGetTags_thenContainsMoodTagsFromResearch() {
        List<String> tags = MoodMapping.tagsFor("Thunderstorm");
        assertTrue(tags.contains("angry"));
        assertTrue(tags.contains("aggressive"));
        assertTrue(tags.contains("tense"));
    }

    @Test
    void givenRainWeather_whenGetTags_thenContainsMoodTagsFromResearch() {
        List<String> tags = MoodMapping.tagsFor("Rain");
        assertTrue(tags.contains("sad"));
        assertTrue(tags.contains("gloomy"));
        assertTrue(tags.contains("sorrow"));
    }

    @Test
    void givenCloudsWeather_whenGetTags_thenContainsMoodTagsFromResearch() {
        List<String> tags = MoodMapping.tagsFor("Clouds");
        assertTrue(tags.contains("relaxed"));
        assertTrue(tags.contains("calm"));
        assertTrue(tags.contains("peaceful"));
    }

    @Test
    void givenAnyWeather_whenGetTags_thenListIsNotEmpty() {
        assertFalse(MoodMapping.tagsFor("Clear").isEmpty());
        assertFalse(MoodMapping.tagsFor("Thunderstorm").isEmpty());
        assertFalse(MoodMapping.tagsFor("Rain").isEmpty());
        assertFalse(MoodMapping.tagsFor("Clouds").isEmpty());
        assertFalse(MoodMapping.tagsFor("Unknown").isEmpty());
    }

    @Test
    void givenMistWeather_whenGetTags_thenReturnsSameSadTagsAsRain() {
        List<String> rainTags = MoodMapping.tagsFor("Rain");
        List<String> mistTags = MoodMapping.tagsFor("Mist");
        assertEquals(rainTags, mistTags);
    }
}
