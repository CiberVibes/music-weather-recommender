package es.ulpgc.dacd.business.recommendation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoodMapperTest {

    @Test
    void givenClearWeather_whenGetMoodName_thenReturnsHappy() {
        String result = MoodMapper.moodName("Clear");
        assertEquals("Happy (Q1)", result);
    }

    @Test
    void givenThunderstormWeather_whenGetMoodName_thenReturnsAngry() {
        String result = MoodMapper.moodName("Thunderstorm");
        assertEquals("Angry (Q2)", result);
    }

    @Test
    void givenRainWeather_whenGetMoodName_thenReturnsSad() {
        String result = MoodMapper.moodName("Rain");
        assertEquals("Sad (Q3)", result);
    }

    @Test
    void givenDrizzleWeather_whenGetMoodName_thenReturnsSad() {
        String result = MoodMapper.moodName("Drizzle");
        assertEquals("Sad (Q3)", result);
    }

    @Test
    void givenSnowWeather_whenGetMoodName_thenReturnsSad() {
        String result = MoodMapper.moodName("Snow");
        assertEquals("Sad (Q3)", result);
    }

    @Test
    void givenFogWeather_whenGetMoodName_thenReturnsSad() {
        String result = MoodMapper.moodName("Fog");
        assertEquals("Sad (Q3)", result);
    }

    @Test
    void givenCloudsWeather_whenGetMoodName_thenReturnsRelaxed() {
        String result = MoodMapper.moodName("Clouds");
        assertEquals("Relaxed (Q4)", result);
    }

    @Test
    void givenUnknownWeather_whenGetMoodName_thenReturnsDefault() {
        String result = MoodMapper.moodName("Tornado");
        assertEquals("Happy (Q1)", result);
    }

    @Test
    void givenLowerCaseWeather_whenGetMoodName_thenReturnsCorrectMood() {
        assertEquals("Happy (Q1)", MoodMapper.moodName("clear"));
        assertEquals("Angry (Q2)", MoodMapper.moodName("thunderstorm"));
        assertEquals("Sad (Q3)", MoodMapper.moodName("rain"));
        assertEquals("Relaxed (Q4)", MoodMapper.moodName("clouds"));
    }

    @Test
    void givenClearWeather_whenGetTags_thenContainsMoodTagsFromResearch() {
        List<String> tags = MoodMapper.tagsFor("Clear");
        assertTrue(tags.contains("happy"));
        assertTrue(tags.contains("cheerful"));
        assertTrue(tags.contains("fun"));
    }

    @Test
    void givenClearWeather_whenGetTags_thenContainsGenreFallbacks() {
        List<String> tags = MoodMapper.tagsFor("Clear");
        assertTrue(tags.contains("pop"));
        assertTrue(tags.contains("dance"));
    }

    @Test
    void givenThunderstormWeather_whenGetTags_thenContainsMoodTagsFromResearch() {
        List<String> tags = MoodMapper.tagsFor("Thunderstorm");
        assertTrue(tags.contains("angry"));
        assertTrue(tags.contains("aggressive"));
        assertTrue(tags.contains("tense"));
    }

    @Test
    void givenRainWeather_whenGetTags_thenContainsMoodTagsFromResearch() {
        List<String> tags = MoodMapper.tagsFor("Rain");
        assertTrue(tags.contains("sad"));
        assertTrue(tags.contains("gloomy"));
        assertTrue(tags.contains("sorrow"));
    }

    @Test
    void givenCloudsWeather_whenGetTags_thenContainsMoodTagsFromResearch() {
        List<String> tags = MoodMapper.tagsFor("Clouds");
        assertTrue(tags.contains("relaxed"));
        assertTrue(tags.contains("calm"));
        assertTrue(tags.contains("peaceful"));
    }

    @Test
    void givenAnyWeather_whenGetTags_thenListIsNotEmpty() {
        assertFalse(MoodMapper.tagsFor("Clear").isEmpty());
        assertFalse(MoodMapper.tagsFor("Thunderstorm").isEmpty());
        assertFalse(MoodMapper.tagsFor("Rain").isEmpty());
        assertFalse(MoodMapper.tagsFor("Clouds").isEmpty());
        assertFalse(MoodMapper.tagsFor("Unknown").isEmpty());
    }

    @Test
    void givenMistWeather_whenGetTags_thenReturnsSameSadTagsAsRain() {
        List<String> rainTags = MoodMapper.tagsFor("Rain");
        List<String> mistTags = MoodMapper.tagsFor("Mist");
        assertEquals(rainTags, mistTags);
    }
}
