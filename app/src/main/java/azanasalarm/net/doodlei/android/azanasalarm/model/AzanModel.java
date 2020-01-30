package azanasalarm.net.doodlei.android.azanasalarm.model;

public class AzanModel {


    String file_name, id, file_uri, title, artist, album;

    public AzanModel(String file_name, String id, String file_uri, String title, String artist, String album) {
        this.file_name = file_name;
        this.id = id;
        this.file_uri = file_uri;
        this.title = title;
        this.artist = artist;
        this.album = album;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFile_uri() {
        return file_uri;
    }

    public void setFile_uri(String file_uri) {
        this.file_uri = file_uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }
}
