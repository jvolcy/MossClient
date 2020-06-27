import java.io.*;
//./moss.pl -l python -c "Assignment x" -b "submission/test_student.py" submission/a01*
//import java.io.File;
//import java.net.URL;
//import java.util.Collection;


public class Main {
    private final static String USER_ID = "456234332";

    public static void main(String[] args) throws Exception {
        System.out.println("Hello World!");

        MossClient client = new MossClient(USER_ID, "python");

        //m.addBaseFile("submission/a01.py")
        client.addBaseFile("submission/test_student.py");

        // Submission Files
        client.addFile("submission/a01-sample.py");
        client.addFile("submission/a01-p146011.py");
        client.addFile("submission/a01-p146099.py");
        client.addFile("submission/a01.py");

        String url = client.send();
        System.out.println("Report URL: " + url);

        System.out.println(client.getLanguages()[4]);

    }
}


/*
import mosspy

userid = 456234332
#./moss.pl -l python -c "Assignment x" -b "submission/test_student.py" submission/a01-*

m = mosspy.Moss(userid, "python")

#m.addBaseFile("submission/a01.py")
m.addBaseFile("submission/test_student.py")

# Submission Files
m.addFile("submission/a01-sample.py")

m.addFilesByWildcard("submission/a01-*.py")

url = m.send()

print ("Report URL: " + url)

# Save report file
m.saveWebPage(url, "submission/report.html")

mosspy.download_report(url, "submission/report/", connections=8, log_level=10) # logging.DEBUG (20 to disable)


*/


