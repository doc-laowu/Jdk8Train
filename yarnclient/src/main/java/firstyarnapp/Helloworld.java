package firstyarnapp;

public class Helloworld {

    public static void main(String[] args) {
        int length = Integer.parseInt(args[0]);
        System.out.println("the length is:"+length);
        for (int i=0; i<length; i++) {
            System.out.println("current index is:" + i);
        }
    }

}
