import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Arrays;
import java.net.*;

//./moss.pl -l python -c "Assignment x" -b "submission/test_student.py" submission/a01*



public class MossClient {
    private String userid;
    private String server;
    private int port;
    private ArrayList<String> files;
    private ArrayList<String> baseFiles;

    //create an options dictionary
    private Hashtable<String, String> options;

    private String[] languages = {
            "c", "cc", "java", "ml", "pascal","ada","lisp", "scheme",
            "haskell", "fortran", "ascii","vhdl", "verilog", "perl",
            "matlab", "python", "mips","prolog",  "spice", "vb",
            "csharp", "modula2", "a8086", "javascript",  "plsql"
    };

    private Socket socket;
    private DataOutputStream socketOutStream;
    private DataInputStream socketInStream;


    public MossClient (String userid) {
        //This overloaded constructor provides default server and port.
        //The user provides a moss userid only.
        this(userid, "moss.stanford.edu", 7690);
    }

    public MossClient(String userid, String server, int port)
    {
        this.userid = userid;
        this.server = server;
        this.port = port;

        //initialize dictionaries
        files = new ArrayList<>();
        baseFiles = new ArrayList<>();

        //set the default options
        //System.out.println(userid + language);
        options = new Hashtable<>();

        options.put("l", "");
        options.put("m", "10");
        options.put("d", "0");
        options.put("x", "0");
        options.put("c", "");
        options.put("n", "250");


        System.out.println("userid set\n");
    }

    public void addFile(String filePath)
    {
        files.add(filePath);
    }

    public void addBaseFile(String filePath)
    {
        baseFiles.add(filePath);
    }


    public void setLanguage(String language){
        //check that the language is in the list
        if (new ArrayList<>(Arrays.asList(languages)).contains(language)) {
            options.put("l", language);
            //this.language = language;
        }
        else {
            options.put("l", "");
            //this.language = "";
        }
    }

    public void setIgnoreLimit(int limit) {
        options.put("m", Integer.toString(limit));
    }

    public void setCommentString(String comment) {
        options.put("c", comment);
    }

    public void setNumberOfMatchingFiles(int n) {
        if (n > 1) {
            options.put("n", Integer.toString(n));
        }
    }

    public void setDirectoryMode(String mode) {
        options.put("d", mode);
    }

    public void setExperimentalServer(String opt) {
        options.put("x", opt);
    }

    public String[] getLanguages() {
        return languages;
    }


    private void openSocket() {
        try {
            System.out.println(String.format("openSocket().  server=%s, port = %d", server, port));
            socket = new Socket(server, port);
            socketOutStream = new DataOutputStream(socket.getOutputStream());
            socketInStream = new DataInputStream(socket.getInputStream());
        }

        catch (Exception e){
            System.out.println("Caught exception: MossClient.openSocket(): " + e.toString());
        }
    }

    private void closeSocket() {
        try {
            socketOutStream.close();
            socketInStream.close();
            socket.close();
        } catch (Exception e) {
            System.out.println("Caught exception: MossClient.closeSocket(): " + e.toString());
        }
    }

    private void socketWrite(String data) {
        //System.out.println("socketWrite(): >>"+data+"<<");

        try {
            byte []bdata = data.getBytes(StandardCharsets.UTF_8);
            //for (byte x : bdata) {System.out.print(x);System.out.print(" ");}
            //System.out.println();
            //System.out.println(bdata.length);

            //socketOutStream.writeUTF(data);
            socketOutStream.write(bdata, 0, bdata.length);

            socketOutStream.flush();
        }
        catch (Exception e){
            System.out.println("Caught exception: MossClient.socketWrite(): " + e.toString());
        }
    }


    private String socketRead(){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socketInStream));
            return (reader.readLine());
        }
/*      try {
            return socketInStream.readUTF();
        }*/
            catch (Exception e){
            System.out.println("Caught exception: MossClient.socketRead(): " + e.toString());
            return "";
        }
    }

    public String send(){
        String recv;
        System.out.println("Opening socket...");
        openSocket();
        System.out.println("Writing parameters...");
        socketWrite(String.format("moss %s\n", userid));
        socketWrite(String.format("directory %s\n", options.get("d")));
        socketWrite(String.format("X %s\n", options.get("x")));
        socketWrite(String.format("maxmatches %s\n", options.get("m")));
        socketWrite(String.format("show %s\n", options.get("n")));
        socketWrite(String.format("language %s\n", options.get("l")));
        System.out.println("Retrieving status confirmation...");
        recv = socketRead();
        if (recv.equals("no")) {
            socketWrite("end\n");
            closeSocket();
            System.out.println("send() => Language not accepted by server");
            return "";
        }
        else
        {
            System.out.println("confirmation >>"+recv+"<<");
        }
        System.out.println("Uploading baseFiles...");
        for (String file_path : baseFiles) {
            //print("sending -->", file_path, display_name)
            uploadFile(file_path, file_path, 0);
        }

        System.out.println("Uploading Files...");
        int index = 1;
        for (String file_path : files) {
            //print("sending -->",file_path, display_name, index )
            uploadFile(file_path, file_path, index);
            index += 1;

        }

        //print("sending --> query 0 {}\n".format(self.options['c']).encode())
        socketWrite(String.format("query 0 %s\n", options.get("c")));

        System.out.println("Retrieving response...");
        String response = socketRead();

        //print("sending --> {}\n".format("end\n").encode())
        socketWrite("end\n");

        //print('*** response=', response)
        closeSocket();

        return response.replace("\n","");
    }



    private void uploadFile(String file_path, String display_name, int file_id) {
        /*Note: socket must be open before calling this function. */
        if (display_name.equals("")) {
            //If no display name added by user,default to file path
            //Display name cannot accept \,replacing it with /
            display_name = file_path.replace(" ", "_").replace("\\", "/");
        }

        String content;
        try {
            Path fileName = Path.of(file_path);
            content = Files.readString(fileName);
        }
        catch (Exception e) {
            System.out.println("Caught exception: MossClient.uploadFile(): " + e.toString());
            content = "";
        }

        long size = content.length();

        String message = String.format("file %d %s %d %s\n",
                file_id,
                options.get("l"),
                size,
                display_name
        );

        socketWrite(message);
        socketWrite(content);

    }

    public String getWebPage(String url) {

        String response = "";
        try {
            URL oracle = new URL(url);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(oracle.openStream()));

            final int BUF_SIZE = 4096;
            char[] buffer = new char[BUF_SIZE];

            while ( in.read(buffer, 0, BUF_SIZE)  != -1) {
                response = response +  (new String(buffer)).trim(); //new String(buffer);
            }

            /*
            String data;
            while ((data = in.readLine() ) != null)
                response = response + data + '\n';
            */
            in.close();
        }
        catch (Exception e){
            System.out.println("Caught exception: MossClient.send(): " + e.toString());
        }

        return(response);
    }

}


