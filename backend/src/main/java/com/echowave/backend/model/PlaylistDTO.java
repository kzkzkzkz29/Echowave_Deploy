package com.echowave.backend.model;

import java.util.List;

public class PlaylistDTO {
    private List<AudioDTO> audios;

    public PlaylistDTO() {
    }

    public PlaylistDTO(List<AudioDTO> audios) {
        this.audios = audios;
    }

    public List<AudioDTO> getAudios() {
        return audios;
    }

    public void setAudios(List<AudioDTO> audios) {
        this.audios = audios;
    }
}
