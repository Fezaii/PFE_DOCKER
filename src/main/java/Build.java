public class Build {

    private final String hash;
    private final String image;
    //private final RandomString id;

    public Build(String hash , String image) {
        this.hash = hash;
        this.image = image;
    }

    public String getHash() { return hash; }
    public String getImage() { return image; }
    //public String getId() {
    //  return id.nextString();
    //}

    public  void ToString(){
        System.out.println(this.hash +" build " + this.image );
    }
}
