import java.io.*;

public class Main {
    private final static String USER_ID = "456234332";

    public static void main(String[] args) throws Exception {

        MossClient client = new MossClient(USER_ID);

        client.setLanguage("python");
        client.setCommentString("Assignment X");

        //m.addBaseFile("submission/a01.py")
        client.addFile("/Users/jvolcy/submission/test_student.py");

        // Submission Files
        client.addFile("/Users/jvolcy/submission/a01-sample.py");
        client.addFile("/Users/jvolcy/submission/a01-p146011.py");
        client.addFile("/Users/jvolcy/submission/a01-p146099.py");
        client.addBaseFile("/Users/jvolcy/submission/a01.py");

        String url = client.send();
        System.out.println("Report URL: " + url);

        //System.out.println(client.getLanguages()[4]);

    }
}

