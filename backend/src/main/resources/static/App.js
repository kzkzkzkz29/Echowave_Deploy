const uploadForm = document.getElementById('uploadForm');
const audioPlayer = document.getElementById('audioPlayer');
const playlistElement = document.getElementById('playlist');

uploadForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fileInput = document.getElementById('audioFile');
    const file = fileInput.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);

    // Subir el archivo
    const upload = await fetch('/api/audio/upload', { method: 'POST', body: formData });
    if (!upload.ok) {
        alert('Error al subir el archivo');
        return;
    }

    const uploadResult = await upload.json();

    // Agregar a la playlist con filename y path
    await fetch('/api/audio/playlist/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
            filename: uploadResult.filename,
            path: uploadResult.tempPath,
        })
    });

    fileInput.value = '';
    loadPlaylist();
});

async function loadPlaylist() {
    const res = await fetch('/api/audio/playlist');
    const data = await res.json();

    playlistElement.innerHTML = '';
    data.forEach((song, index) => {
        const li = document.createElement('li');
        li.className = 'flex justify-between items-center bg-gray-800 p-2 rounded song';

        li.innerHTML = `
            <span>${song.filename}</span>
            <div class="space-x-2">
                <button onclick="play(${index})" class="text-green-400">▶️</button>
                <button onclick="remove(${index})" class="text-red-400">❌</button>
            </div>
        `;

        playlistElement.appendChild(li);
    });
}

async function play(index) {
    audioPlayer.src = `/api/audio/playlist/play/${index}`;
    audioPlayer.play();
}

async function remove(index) {
    await fetch(`/api/audio/playlist/remove/${index}`, { method: 'DELETE' });
    loadPlaylist();
}

async function shuffle() {
    await fetch('/api/audio/playlist/shuffle', { method: 'POST' });
    loadPlaylist();
}

async function repeat() {
    const res = await fetch('/api/audio/playlist');
    const data = await res.json();
    if (data.length === 0) return;
    const index = 0;
    audioPlayer.src = `/api/audio/playlist/play/${index}?repeat=true`;
    audioPlayer.loop = true;
    audioPlayer.play();
}

loadPlaylist();
