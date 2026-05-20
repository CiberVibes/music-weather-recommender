'use strict';

let player = null;
let deviceId = null;
let sdkReady = false;
let spotifyToken = null;
let currentLocation = null;

// ── Init ─────────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
    loadLocations();
    setInterval(loadLocations, 5 * 60 * 1000);
    checkSpotifyAuth();
});

window.onSpotifyWebPlaybackSDKReady = () => {
    sdkReady = true;
    if (spotifyToken) initPlayer();
};

// ── Locations ─────────────────────────────────────────────────────────────────

async function loadLocations() {
    const res = await fetch('/api/locations');
    const locations = await res.json();
    const list = document.getElementById('locations-list');
    list.textContent = '';

    if (!locations.length) {
        const p = document.createElement('p');
        p.className = 'hint';
        p.textContent = 'Waiting for weather data...';
        list.appendChild(p);
        return;
    }

    locations.forEach(loc => {
        const card = document.createElement('div');
        card.className = 'location-card';
        card.dataset.location = loc.location;

        const name = document.createElement('div');
        name.className = 'location-name';
        name.textContent = loc.location;

        const meta = document.createElement('div');
        meta.className = 'location-meta';

        const condBadge = document.createElement('span');
        condBadge.className = 'condition-badge';
        condBadge.textContent = loc.condition;

        const moodBadge = document.createElement('span');
        moodBadge.className = 'mood-badge';
        moodBadge.textContent = loc.mood;

        meta.appendChild(condBadge);
        meta.appendChild(moodBadge);
        card.appendChild(name);
        card.appendChild(meta);

        card.addEventListener('click', () => selectLocation(loc.location, loc.condition, loc.mood, card));
        list.appendChild(card);
    });

    if (currentLocation) {
        const card = list.querySelector(`[data-location="${CSS.escape(currentLocation)}"]`);
        if (card) card.classList.add('selected');
    }
}

// ── Recommendations ───────────────────────────────────────────────────────────

async function selectLocation(location, condition, mood, card) {
    currentLocation = location;

    document.querySelectorAll('.location-card').forEach(c => c.classList.remove('selected'));
    card.classList.add('selected');

    document.getElementById('recs-title').textContent = `${location} — ${condition} · ${mood}`;

    const list = document.getElementById('tracks-list');
    list.textContent = '';
    const loading = document.createElement('p');
    loading.className = 'hint';
    loading.textContent = 'Loading...';
    list.appendChild(loading);

    const res = await fetch(`/api/recommendations?location=${encodeURIComponent(location)}`);
    const tracks = await res.json();

    list.textContent = '';

    if (!tracks.length) {
        const p = document.createElement('p');
        p.className = 'hint';
        p.textContent = 'No recommendations yet for this location.';
        list.appendChild(p);
        return;
    }

    tracks.forEach((track, i) => list.appendChild(buildTrackItem(track, i + 1)));
}

function buildTrackItem(track, rank) {
    const item = document.createElement('div');
    item.className = 'track-item';

    const rankWrap = document.createElement('div');
    rankWrap.className = 'track-rank-wrap';

    const rankSpan = document.createElement('span');
    rankSpan.className = 'track-rank';
    rankSpan.textContent = String(rank);

    const hoverBtn = document.createElement('button');
    hoverBtn.className = 'play-track-btn';
    hoverBtn.title = 'Play';
    hoverBtn.textContent = '▶';

    rankWrap.appendChild(rankSpan);
    rankWrap.appendChild(hoverBtn);

    const info = document.createElement('div');
    info.className = 'track-info';

    const nameEl = document.createElement('div');
    nameEl.className = 'track-name';
    nameEl.textContent = track.name;

    const artistEl = document.createElement('div');
    artistEl.className = 'track-artist';
    artistEl.textContent = track.artist;

    info.appendChild(nameEl);
    info.appendChild(artistEl);

    const playBtn = document.createElement('button');
    playBtn.className = 'track-play-btn';
    playBtn.title = 'Play on Spotify';
    playBtn.textContent = '▶';

    const onPlay = () => searchAndPlay(track.name, track.artist);
    hoverBtn.addEventListener('click', onPlay);
    playBtn.addEventListener('click', onPlay);

    item.appendChild(rankWrap);
    item.appendChild(info);
    item.appendChild(playBtn);
    return item;
}

// ── Spotify Auth ──────────────────────────────────────────────────────────────

function connectSpotify() {
    window.location.href = '/auth/login';
}

async function checkSpotifyAuth() {
    const res = await fetch('/api/token');
    if (!res.ok) return;
    spotifyToken = (await res.json()).access_token;
    onSpotifyAuthorized();
}

function onSpotifyAuthorized() {
    document.getElementById('connect-btn').classList.add('hidden');
    document.getElementById('user-info').classList.remove('hidden');
    if (sdkReady) initPlayer();
}

async function getToken() {
    if (spotifyToken) return spotifyToken;
    const res = await fetch('/api/token');
    if (!res.ok) return null;
    spotifyToken = (await res.json()).access_token;
    return spotifyToken;
}

// ── Web Playback SDK ──────────────────────────────────────────────────────────

function initPlayer() {
    player = new Spotify.Player({
        name: 'Music Weather Recommender',
        getOAuthToken: async (cb) => cb(await getToken()),
        volume: 0.5
    });

    player.addListener('ready', ({ device_id }) => {
        deviceId = device_id;
        document.getElementById('player-bar').classList.remove('hidden');
    });

    player.addListener('not_ready', () => {
        deviceId = null;
        setTimeout(() => player.connect(), 1000);
    });

    player.addListener('player_state_changed', state => {
        if (!state) return;
        const track = state.track_window.current_track;
        document.getElementById('now-track').textContent = track.name;
        document.getElementById('now-artist').textContent = track.artists.map(a => a.name).join(', ');

        const art = document.getElementById('album-art');
        if (track.album.images.length) {
            art.src = track.album.images[0].url;
            art.classList.remove('hidden');
        }

        document.getElementById('play-pause-btn').textContent = state.paused ? '▶' : '⏸';
    });

    player.addListener('account_error', ({ message }) => {
        alert('Spotify Premium required for playback:\n' + message);
    });

    player.connect();
}

// ── Playback ──────────────────────────────────────────────────────────────────

async function searchAndPlay(name, artist) {
    if (!deviceId) { alert('Spotify player not ready — wait a moment and try again.'); return; }

    const searchRes = await fetch(`/api/search?name=${encodeURIComponent(name)}&artist=${encodeURIComponent(artist)}`);
    if (searchRes.status === 400 || searchRes.status === 401) {
        alert('Spotify not connected — click "Connect Spotify" and authorize again.');
        return;
    }
    if (!searchRes.ok) {
        const errText = await searchRes.text();
        alert('Search error ' + searchRes.status + ': ' + errText);
        return;
    }
    const { uri } = await searchRes.json();

    const playRes = await fetch('/api/play', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ deviceId, uri })
    });
    if (!playRes.ok) {
        const text = await playRes.text();
        alert('Play failed (' + playRes.status + '): ' + text);
    }
}

async function togglePlay() { if (player) await player.togglePlay(); }
async function prevTrack() { if (player) await player.previousTrack(); }
async function nextTrack() { if (player) await player.nextTrack(); }
async function setVolume(val) { if (player) await player.setVolume(parseFloat(val)); }
