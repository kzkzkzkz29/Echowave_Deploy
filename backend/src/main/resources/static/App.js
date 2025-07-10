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

    // Subir archivo
    const upload = await fetch('http://localhost:8080/api/archivo/upload', {
        method: 'POST',
        body: formData,
        credentials: 'include'
    });

    if (!upload.ok) {
        alert('Error al subir el archivo');
        return;
    }

    const uploadResult = await upload.json();

    // Agregar a playlist
    const agregar = await fetch('http://localhost:8080/api/playlist/agregar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify({
            fileName: uploadResult.filename,
            url: uploadResult.path,
        })
    });

    if (!agregar.ok) {
        const errorText = await agregar.text();
        alert('Error al agregar a la playlist:\n' + errorText);
        console.error(errorText);
        return;
    }


    fileInput.value = '';
    loadPlaylist();
});

async function loadPlaylist() {
    const res = await fetch('http://localhost:8080/api/playlist', {
        credentials: 'include'
    });

    const data = await res.json();
    playlistElement.innerHTML = '';

    data.forEach((song, index) => {
        const li = document.createElement('li');
        li.className = 'flex justify-between items-center bg-gray-800 p-2 rounded song';

        li.innerHTML = `
            <span>${song.fileName}</span>
            <div class="space-x-2">
                <button onclick="play(${index})" class="text-green-400">▶️</button>
                <button onclick="remove(${index})" class="text-red-400">❌</button>
            </div>
        `;

        playlistElement.appendChild(li);
    });
}

async function play(index) {
    const res = await fetch('http://localhost:8080/api/playlist', {
        credentials: 'include'
    });

    const data = await res.json();
    if (!data[index]) return;

    const song = data[index];
    audioPlayer.loop = false;
    audioPlayer.src = song.url; // Reproducimos directamente el link de Firebase
    audioPlayer.play();
}

async function remove(index) {
    await fetch(`http://localhost:8080/api/playlist/eliminar/${index}`, {
        method: 'DELETE',
        credentials: 'include'
    });
    loadPlaylist();
}

async function shuffle() {
    await fetch('http://localhost:8080/api/playlist/mezclar', {
        method: 'POST',
        credentials: 'include'
    });
    loadPlaylist();
}

async function repeat() {
    const res = await fetch('http://localhost:8080/api/playlist', {
        credentials: 'include'
    });
    const data = await res.json();
    if (data.length === 0) return;

    const index = 0;
    audioPlayer.src = `http://localhost:8080/api/reproductor/playlist/${index}?repeat=true`;
    await fetch(audioPlayer.src, { credentials: 'include' }); // Asegura la sesión
    audioPlayer.loop = true;
    audioPlayer.play();
}

loadPlaylist();
