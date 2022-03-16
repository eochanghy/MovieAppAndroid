package fu.prm391.sampl.finalproject_movieapp.Model;

public class VideoUploadDetails {
    private String videoSlide, videoType, videoThumb, videoUrl, videoName, videoDescription, videoCategory;

    public VideoUploadDetails(String videoSlide, String videoType, String videoThumb, String videoUrl, String videoName, String videoDescription, String videoCategory) {
        this.videoSlide = videoSlide;
        this.videoType = videoType;
        this.videoThumb = videoThumb;
        this.videoUrl = videoUrl;
        this.videoName = videoName;
        this.videoDescription = videoDescription;
        this.videoCategory = videoCategory;
    }

    public VideoUploadDetails() {
    }

    public String getVideoSlide() {
        return videoSlide;
    }

    public void setVideoSlide(String videoSlide) {
        this.videoSlide = videoSlide;
    }

    public String getVideoType() {
        return videoType;
    }

    public void setVideoType(String videoType) {
        this.videoType = videoType;
    }

    public String getVideoThumb() {
        return videoThumb;
    }

    public void setVideoThumb(String videoThumb) {
        this.videoThumb = videoThumb;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoDescription() {
        return videoDescription;
    }

    public void setVideoDescription(String videoDescription) {
        this.videoDescription = videoDescription;
    }

    public String getVideoCategory() {
        return videoCategory;
    }

    public void setVideoCategory(String videoCategory) {
        this.videoCategory = videoCategory;
    }
}
