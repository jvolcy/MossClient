import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Arrays;
import java.net.*;

/* ======================================================================
 * This file implements a client for the Stanford MOSS (Measurement Of
 * Software Similarity) based on the python client provided by
 * Syed Owais Ali Chishti and available at
 * //https://github.com/soachishti/moss.py
 *
 * The MOSS homesite is
 * http://theory.stanford.edu/~aiken/moss/
 * or
 * http://moss.stanford.edu
 *
 * MOSS is an online service.  You must register with the website to
 * obtain a userid number (typically a 9-digit number) before you can
 * use this class.
 *
 *
 * class Usage
 * -----------
 * 1) You must specify the moss userid when a MossClient object is
 * instantiated.
 * 2) Specify all options for the code comparison.  At a minimum, you
 * must specify the coding language (setLanguage()).  Use the getLanguages()
 * method to retrieve a list of supported languages.
 * 3) You may optionally specify other options using the
 * setCommentString(), setNumberOfMatchingFiles(), setDirectoryMode()
 * and setExperimentalServer() functions.
 * 4) Upload any base files.  These are "starter code" files that may
 * have been provided to the students.  Call the addBaseFile() method
 * for each base file to be uploaded.
 * 5) Upload student files to be compared.  Call the addFile() method
 * for each student file to be uploaded.
 * 6) Call send().  send() logs in to the server, communicates the
 * selected options, transfers the base and student files and returns
 * the result URL unless an error occurs.
 * 7) Use the getWebPage() to download the HTML report, if desired.
 * ===================================================================== */



/* ======================================================================
 * MossClient
 * ===================================================================== */
public class MossClient {
    //---------- class members ----------
    private static final String DEFAULT_MOSS_SERVER = "moss.stanford.edu";
    private static final int DEFAULT_MOSS_PORT = 7690;

    private String userid;      //moss userid
    private String server;      //moss server
    private int port;           //moss server port
    private Socket socket;      //moss server socket object
    private DataOutputStream socketOutStream;   //moss socket output stream
    private DataInputStream socketInStream;     //moss socket input stream

    private ArrayList<String> files;    //list of student files
    private ArrayList<String> baseFiles;    //list of base files.  These are files provided to students as "starter code".
    private Hashtable<String, String> options;      //options dictionary.  These are options passed to the MOSS server

    private final String[] languages = {        //array of valid languages MOSS can work with
            "c", "cc", "java", "ml", "pascal","ada","lisp", "scheme",
            "haskell", "fortran", "ascii","vhdl", "verilog", "perl",
            "matlab", "python", "mips","prolog",  "spice", "vb",
            "csharp", "modula2", "a8086", "javascript",  "plsql" };


    /* ======================================================================
     * Constructor with user id only
     * Use this overloaded constructor to use the default MOSS server and port #.
     * The user provides a moss userid only.
     * ===================================================================== */
    MossClient(String userid) {
        //call the general constructor with default server and port #s.
        this(userid, DEFAULT_MOSS_SERVER, DEFAULT_MOSS_PORT);
    }


    /* ======================================================================
    * General Constructor
    * Use this constructor if you want to specify a moss server and port other
    * than the defaults.  This function is provided only in case the MOSS
    * administrators change the name and/or port number of the service in the
    * future.
    * ===================================================================== */
    public MossClient(String userid, String server, int port)
    {
        //store the userid, server and port #
        this.userid = userid;
        this.server = server;
        this.port = port;

        //initialize the base file and student submission files dictionaries
        files = new ArrayList<>();
        baseFiles = new ArrayList<>();

        //set the default options
        //System.out.println(userid + language);
        options = new Hashtable<>();

        options.put("l", "");       //seleced language
        options.put("m", "10");     //the maximum number of times a given passage may appear before it is ignored
        options.put("d", "0");      //The -d option specifies that submissions are by directory, not by file.
                                    //That is, files in a directory are taken to be part of the same program,
        options.put("x", "0");      //The -x option sends queries to the current experimental version of the server.
        options.put("c", "");       //The -c option supplies a comment string that is attached to the generated report.
        options.put("n", "250");    //the number of matching files to show in the results
    }

    /* ======================================================================
     * Use addFile to add a student programming file to the list of files
     * to be compared.
     * ===================================================================== */
    void addFile(String filePath)
    {
        files.add(filePath);
    }

    /* ======================================================================
     * Moss normally reports all code
     * that matches in pairs of files.  When a base file is supplied,
     * program code that also appears in the base file is not counted in matches.
     * A typical base file will include, for example, the instructor-supplied
     * code for an assignment.  Multiple base files are allowed.  You should
     * use a base file if it is convenient; base files improve results, but
     * are not usually necessary for obtaining useful information.
     * The -b option names a "base file" when the request is sent to the server.
     * ===================================================================== */
    void addBaseFile(String filePath)
    {
        baseFiles.add(filePath);
    }

    /* ======================================================================
     * The -l option specifies the source language of the tested programs.
     * Moss supports many different languages; see the variable "languages" for the
     * full list.
     * ===================================================================== */
    void setLanguage(String language){
        //check that the language is in the list
        if (new ArrayList<>(Arrays.asList(languages)).contains(language)) {
            options.put("l", language);
        }
        else {
            options.put("l", "");
        }
    }

    /* ======================================================================
    # The -m option sets the maximum number of times a given passage may appear
    # before it is ignored.  A passage of code that appears in many programs
    # is probably legitimate sharing and not the result of plagiarism.  With -m N,
    # any passage appearing in more than N programs is treated as if it appeared in
    # a base file (i.e., it is never reported).  Option -m can be used to control
    # moss' sensitivity.  With -m 2, moss reports only passages that appear
    # in exactly two programs.  If one expects many very similar solutions
    # (e.g., the short first assignments typical of introductory programming
    # courses) then using -m 3 or -m 4 is a good way to eliminate all but
    # truly unusual matches between programs while still being able to detect
    # 3-way or 4-way plagiarism.  With -m 1000000 (or any very
    # large number), moss reports all matches, no matter how often they appear.
    # The -m setting is most useful for large assignments where one also a base file
    # expected to hold all legitimately shared code.  The default for -m is 10.
     * ===================================================================== */
    public void setIgnoreLimit(int limit) {
        options.put("m", Integer.toString(limit));
    }

    /* ======================================================================
     # The -c option supplies a comment string that is attached to the generated
    # report.  This option facilitates matching queries submitted with replies
    # received, especially when several queries are submitted at once.
     * ===================================================================== */
    void setCommentString(String comment) {
        options.put("c", comment);
    }

    /* ======================================================================
    # The -n option determines the number of matching files to show in the results.
    # The default is 250.
     * ===================================================================== */
    public void setNumberOfMatchingFiles(int n) {
        if (n > 1) {
            options.put("n", Integer.toString(n));
        }
    }

    /* ======================================================================
     * The -d option specifies that submissions are by directory, not by file.
     * That is, files in a directory are taken to be part of the same program,
     * and reported matches are organized accordingly by directory.
     * ===================================================================== */
    public void setDirectoryMode(String mode) {
        options.put("d", mode);
    }

    /* ======================================================================
    # The -x option sends queries to the current experimental version of the server.
    # The experimental server has the most recent Moss features and is also usually
    # less stable (read: may have more bugs).
     * ===================================================================== */
    public void setExperimentalServer(String opt) {
        options.put("x", opt);
    }

    /* ======================================================================
     * Returns the list of language with MOSS can work with.
     * ===================================================================== */
    public String[] getLanguages() {
        return languages;
    }


    /* ======================================================================
     * open the socket connection to the moss server
     * Do not call this function directly.  It is called by the send() function.
     * ===================================================================== */
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

    /* ======================================================================
     * close the socket connection to the moss server
     * Do not call this function directly.  It is called by the send() function.
     * ===================================================================== */
    private void closeSocket() {
        try {
            socketOutStream.close();
            socketInStream.close();
            socket.close();
        } catch (Exception e) {
            System.out.println("Caught exception: MossClient.closeSocket(): " + e.toString());
        }
    }

    /* ======================================================================
     * socketWrite()
     * writes the provided string to the moss server.
     * Note that the socket must be open (see openSocket()) before
     * using this function.
     * Do not call this function directly.  It is called by the send() and
     * uploadFile() functions.
     * ===================================================================== */
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


    /* ======================================================================
     * socketRead()
     * Read a string from the moss socket.
     * Note that the socket must be open (see openSocket()) before
     * using this function.
     * Do not call this function directly.  It is called by the send() function.
     * ===================================================================== */
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


    /* ======================================================================
     * Use the uploadFile() function to upload student programming files to
     * the moss server.
     * Do not call this function directly.  It is called by the send() function.
     * ===================================================================== */
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

    /* ======================================================================
     * The send() function is the central function of the MossClient
     * class, from the class user's perspective.
     * send() performs all transfers to and from the server apart from
     * transferring resulting HTML from the server via the getWebPage()
     * method.
     * Call the send() function after all moss parameters have been set
     * (-l, -m, -x, etc...) and all base files and student program files
     * have been specified (addBaseFile() and addFile(), respectively.)
     * ===================================================================== */
    String send(){
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



    /* ======================================================================
     * Retrieves the results page from the moss server.  Call this function
     * after a successful call to send() to download the comparison report
     * summary page.
     * ===================================================================== */
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


/* ======================================================================
 *
 * ===================================================================== */

//----------  ----------
//----------  ----------
//----------  ----------
//----------  ----------