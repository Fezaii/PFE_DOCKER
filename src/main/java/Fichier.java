public class Fichier {

    private final String filename;
    private final String image;
    private  final String hash;
    private final String repo;
    private  final String path;
    //private final RandomString id;

    public Fichier(String filename , String image,String hash,String repo,String path) {
        this.filename = filename;
        this.image = image;
        this.hash = hash;
        this.repo = repo;
        this.path = path;
    }

    public String getName() { return filename; }
    public String getImage() { return image; }
    public String getHash() { return hash; }

    public String getRepo() {
        return repo;
    }

    public String getPath() {
        return path;
    }
    /*public String getId() {
        return id.nextString();
    }*/

    public  void ToString(){
        System.out.println(this.filename +" FROM " + this.image + "  et son hash est  " + this.hash + "  recupere du repo " + this.repo + "  et le path " + this.path);
    }
}
