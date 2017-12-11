public class From {

    private final String img1;
    private final String img2;
    //private final RandomString id;

    public From(String img1 , String img2) {
        this.img1 = img1;
        this.img2 = img2;
    }

    public String getImg1() { return img1; }
    public String getImg2() { return img2; }
    //public String getId() {
      //  return id.nextString();
    //}

    public  void ToString(){
        System.out.println(this.img1 +" FROM " + this.img2 );
    }
}
