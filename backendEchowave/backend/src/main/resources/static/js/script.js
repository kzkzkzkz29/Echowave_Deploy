document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const uploadForm = document.getElementById('uploadForm');
    const audioFileInput = document.getElementById('audioFile');
    const fileNameSpan = document.getElementById('fileName');
    const uploadBtn = document.getElementById('uploadBtn');
    const messageDiv = document.getElementById('message');
    const progressContainer = document.getElementById('uploadProgress');
    const progressBar = document.getElementById('progressBar');
    const progressText = document.getElementById('progressText');
    const playerSection = document.getElementById('playerSection');
    const audioPlayer = document.getElementById('audioPlayer');
    const playPauseBtn = document.getElementById('playPauseBtn');
    const progressSlider = document.getElementById('progressSlider');
    const currentTimeSpan = document.getElementById('currentTime');
    const durationSpan = document.getElementById('duration');
    const volumeSlider = document.getElementById('volumeSlider');
    const muteBtn = document.getElementById('muteBtn');
    const trackTitle = document.getElementById('trackTitle');
    const trackDetails = document.getElementById('trackDetails');
    const downloadBtn = document.getElementById('downloadBtn');
    const metadataBtn = document.getElementById('metadataBtn');
    const metadataModal = document.getElementById('metadataModal');
    const metadataDisplay = document.getElementById('metadataDisplay');
    const helpBtn = document.getElementById('helpBtn');
    const helpModal = document.getElementById('helpModal');
    const sessionStatus = document.getElementById('sessionStatus');
    const dropZone = document.getElementById('dropZone');
    const closeModals = document.querySelectorAll('.close-modal');

    // Global variables
    let currentFile = null;
    let isDragging = false;

    // Initialize the application
    init();

    function init() {
        setupEventListeners();
        updateSessionStatus();
    }

    function setupEventListeners() {
        // File input change
        audioFileInput.addEventListener('change', handleFileSelect);

        // Form submission
        uploadForm.addEventListener('submit', handleUpload);

        // Drag and drop events
        dropZone.addEventListener('dragover', handleDragOver);
        dropZone.addEventListener('dragleave', handleDragLeave);
        dropZone.addEventListener('drop', handleDrop);

        // Audio player events
        audioPlayer.addEventListener('loadedmetadata', updatePlayerInfo);
        audioPlayer.addEventListener('timeupdate', updateProgress);
        audioPlayer.addEventListener('ended', handlePlaybackEnd);
        audioPlayer.addEventListener('volumechange', updateVolumeIcon);

        // Player control events
        playPauseBtn.addEventListener('click', togglePlayPause);
        progressSlider.addEventListener('input', seekAudio);
        volumeSlider.addEventListener('input', changeVolume);
        muteBtn.addEventListener('click', toggleMute);

        // Button events
        downloadBtn.addEventListener('click', downloadAudio);
        metadataBtn.addEventListener('click', showMetadata);
        helpBtn.addEventListener('click', () => showModal(helpModal));

        // Modal events
        closeModals.forEach(btn => {
            btn.addEventListener('click', () => {
                metadataModal.classList.add('hidden');
                helpModal.classList.add('hidden');
            });
        });

        // Click outside modal to close
        window.addEventListener('click', (e) => {
            if (e.target === metadataModal) {
                metadataModal.classList.add('hidden');
            }
            if (e.target === helpModal) {
                helpModal.classList.add('hidden');
            }
        });
    }

    function handleFileSelect() {
        if (audioFileInput.files.length > 0) {
            currentFile = audioFileInput.files[0];
            fileNameSpan.textContent = currentFile.name;
            uploadBtn.disabled = false;
        } else {
            resetFileInput();
        }
    }

    function handleDragOver(e) {
        e.preventDefault();
        e.stopPropagation();
        dropZone.classList.add('active');
        isDragging = true;
    }

    function handleDragLeave(e) {
        e.preventDefault();
        e.stopPropagation();
        if (!isDragging) {
            dropZone.classList.remove('active');
        }
    }

    function handleDrop(e) {
        e.preventDefault();
        e.stopPropagation();
        dropZone.classList.remove('active');
        isDragging = false;

        if (e.dataTransfer.files.length) {
            audioFileInput.files = e.dataTransfer.files;
            handleFileSelect();
        }
    }

    async function handleUpload(e) {
        e.preventDefault();

        if (!currentFile) {
            showMessage('Please select a file first', 'error');
            return;
        }

        // Validate file type
        const validTypes = ['audio/mpeg', 'audio/wav', 'audio/ogg', 'audio/aac', 'audio/webm'];
        if (!validTypes.includes(currentFile.type)) {
            showMessage('Invalid file type. Please upload an audio file (MP3, WAV, OGG, AAC)', 'error');
            return;
        }

        // Validate file size (100MB)
        if (currentFile.size > 100 * 1024 * 1024) {
            showMessage('File size exceeds 100MB limit', 'error');
            return;
        }

        const formData = new FormData();
        formData.append('file', currentFile);

        try {
            // Show upload progress
            progressContainer.classList.remove('hidden');
            messageDiv.classList.add('hidden');

            const xhr = new XMLHttpRequest();
            xhr.open('POST', '/api/audio/upload', true);

            // Upload progress
            xhr.upload.onprogress = function(e) {
                if (e.lengthComputable) {
                    const percentComplete = Math.round((e.loaded / e.total) * 100);
                    progressBar.style.width = percentComplete + '%';
                    progressText.textContent = percentComplete + '%';
                }
            };

            xhr.onload = function() {
                if (xhr.status === 200) {
                    const response = JSON.parse(xhr.responseText);
                    showMessage(response.message, 'success');
                    setupAudioPlayer(response.filename, currentFile.size);
                    updateSessionStatus('Active');
                } else {
                    const error = JSON.parse(xhr.responseText);
                    showMessage(error.message || 'Upload failed', 'error');
                }
                progressContainer.classList.add('hidden');
            };

            xhr.onerror = function() {
                showMessage('Upload failed. Please try again.', 'error');
                progressContainer.classList.add('hidden');
            };

            xhr.send(formData);

        } catch (error) {
            showMessage('An error occurred: ' + error.message, 'error');
            progressContainer.classList.add('hidden');
        }
    }

    function setupAudioPlayer(filename, fileSize) {
        playerSection.classList.remove('hidden');
        trackTitle.textContent = filename;
        trackDetails.textContent = formatFileSize(fileSize);

        // Add cache busting to force reload
        audioPlayer.src = '/api/audio/stream?' + new Date().getTime();
        audioPlayer.load();

        // Enable controls
        playPauseBtn.disabled = false;
        progressSlider.disabled = false;
        volumeSlider.disabled = false;
        muteBtn.disabled = false;
        downloadBtn.disabled = false;
    }

    function updatePlayerInfo() {
        durationSpan.textContent = formatTime(audioPlayer.duration);
        progressSlider.max = Math.floor(audioPlayer.duration);
    }

    function updateProgress() {
        currentTimeSpan.textContent = formatTime(audioPlayer.currentTime);
        progressSlider.value = Math.floor(audioPlayer.currentTime);
    }

    function updateVolumeIcon() {
        if (audioPlayer.muted || audioPlayer.volume === 0) {
            muteBtn.innerHTML = '<i class="fas fa-volume-mute"></i>';
        } else if (audioPlayer.volume < 0.5) {
            muteBtn.innerHTML = '<i class="fas fa-volume-down"></i>';
        } else {
            muteBtn.innerHTML = '<i class="fas fa-volume-up"></i>';
        }
    }

    function togglePlayPause() {
        if (audioPlayer.paused) {
            audioPlayer.play();
            playPauseBtn.innerHTML = '<i class="fas fa-pause"></i>';
        } else {
            audioPlayer.pause();
            playPauseBtn.innerHTML = '<i class="fas fa-play"></i>';
        }
    }

    function seekAudio() {
        audioPlayer.currentTime = progressSlider.value;
    }

    function changeVolume() {
        audioPlayer.volume = volumeSlider.value;
        audioPlayer.muted = false;
        updateVolumeIcon();
    }

    function toggleMute() {
        audioPlayer.muted = !audioPlayer.muted;
        updateVolumeIcon();
    }

    function handlePlaybackEnd() {
        playPauseBtn.innerHTML = '<i class="fas fa-play"></i>';
    }

    function downloadAudio() {
        const a = document.createElement('a');
        a.href = audioPlayer.src;
        a.download = trackTitle.textContent || 'echowave_audio';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
    }

    async function showMetadata() {
        try {
            const response = await fetch('/api/audio/metadata');
            const data = await response.json();

            metadataDisplay.innerHTML = '';

            if (data.status === 'available') {
                for (const [key, value] of Object.entries(data)) {
                    if (key !== 'status' && key !== 'application' && key !== 'version') {
                        const dt = document.createElement('dt');
                        dt.textContent = formatKey(key);

                        const dd = document.createElement('dd');
                        dd.textContent = formatValue(key, value);

                        metadataDisplay.appendChild(dt);
                        metadataDisplay.appendChild(dd);
                    }
                }
            } else {
                const message = document.createElement('p');
                message.textContent = 'No audio file loaded in current session';
                metadataDisplay.appendChild(message);
            }

            showModal(metadataModal);
        } catch (error) {
            console.error('Error fetching metadata:', error);
        }
    }

    function showModal(modal) {
        modal.classList.remove('hidden');
    }

    function showMessage(text, type) {
        messageDiv.textContent = text;
        messageDiv.className = 'message ' + type;
        messageDiv.classList.remove('hidden');
    }

    function updateSessionStatus(status) {
        if (status) {
            sessionStatus.textContent = status;
            sessionStatus.style.backgroundColor = status === 'Active' ? 'rgba(67, 97, 238, 0.1)' : 'rgba(248, 113, 113, 0.1)';
        }
    }

    function resetFileInput() {
        currentFile = null;
        fileNameSpan.textContent = 'Choose or drag an audio file';
        uploadBtn.disabled = true;
    }

    // Helper functions
    function formatTime(seconds) {
        const minutes = Math.floor(seconds / 60);
        const secs = Math.floor(seconds % 60);
        return `${minutes}:${secs < 10 ? '0' : ''}${secs}`;
    }

    function formatFileSize(bytes) {
        if (bytes < 1024) return bytes + ' bytes';
        else if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
        else return (bytes / 1048576).toFixed(1) + ' MB';
    }

    function formatKey(key) {
        return key.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase());
    }

    function formatValue(key, value) {
        if (key === 'lastModified') {
            return new Date(parseInt(value)).toLocaleString();
        }
        if (key === 'size') {
            return formatFileSize(value);
        }
        return value;
    }
});