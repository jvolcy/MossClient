import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Arrays;

//./moss.pl -l python -c "Assignment x" -b "submission/test_student.py" submission/a01*

//import java.io.File;
//import java.net.URL;
//import java.util.Collection;


public class MossClient {
    private String userid;
    //private String language;
    private ArrayList<String> files;
    private ArrayList<String> baseFiles;

    //create an options dictionary
    private Hashtable<String, String> options;

    private String[] languages = {
            "c",
            "cc",
            "java",
            "ml",
            "pascal",
            "ada",
            "lisp",
            "scheme",
            "haskell",
            "fortran",
            "ascii",
            "vhdl",
            "verilog",
            "perl",
            "matlab",
            "python",
            "mips",
            "prolog",
            "spice",
            "vb",
            "csharp",
            "modula2",
            "a8086",
            "javascript",
            "plsql"};

    public MossClient(String userid, String language)
    {
        this.userid = userid;
        //this.language = language;


        //initialize dictionaries
        files = new ArrayList<String>();
        baseFiles = new ArrayList<String>();

        //set the default options
        //System.out.println(userid + language);
        options = new Hashtable<String, String>();

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
        if (new ArrayList<String>(Arrays.asList(languages)).contains(language)) {
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





    public String send()
    {
        return "x";
    }
}


/*
import os
import socket
import glob
import logging

try:
    from urllib.request import urlopen
except ImportError:
    from urllib2 import urlopen

class Moss:
    languages = (
        "c",
        "cc",
        "java",
        "ml",
        "pascal",
        "ada",
        "lisp",
        "scheme",
        "haskell",
        "fortran",
        "ascii",
        "vhdl",
        "verilog",
        "perl",
        "matlab",
        "python",
        "mips",
        "prolog",
        "spice",
        "vb",
        "csharp",
        "modula2",
        "a8086",
        "javascript",
        "plsql")
    server = 'moss.stanford.edu'
    port = 7690

    def __init__(self, user_id, language="c"):
        self.user_id = user_id
        self.options = {
            "l": "c",
            "m": 10,
            "d": 0,
            "x": 0,
            "c": "",
            "n": 250
        }
        self.base_files = []
        self.files = []

        if language in self.languages:
            self.options["l"] = language

    def setIgnoreLimit(self, limit):
        self.options['m'] = limit

    def setCommentString(self, comment):
        self.options['c'] = comment

    def setNumberOfMatchingFiles(self, n):
        if n > 1:
            self.options['n'] = n

    def setDirectoryMode(self, mode):
        self.options['d'] = mode

    def setExperimentalServer(self, opt):
        self.options['x'] = opt

    def addBaseFile(self, file_path, display_name=None):
        if os.path.isfile(file_path) and os.path.getsize(file_path) > 0:
            self.base_files.append((file_path, display_name))
        else:
            raise Exception("addBaseFile({}) => File not found or is empty.".format(file_path))

    def addFile(self, file_path, display_name=None):
        if os.path.isfile(file_path) and os.path.getsize(file_path) > 0:
            self.files.append((file_path, display_name))
        else:
            raise Exception("addFile({}) => File not found or is empty.".format(file_path))

    def addFilesByWildcard(self, wildcard):
        for file in glob.glob(wildcard, recursive=True):
            self.files.append((file, None))

    def getLanguages(self):
        return self.languages

    def uploadFile(self, s, file_path, display_name, file_id):
        if display_name is None:
            # If no display name added by user, default to file path
            # Display name cannot accept \, replacing it with /
            display_name = file_path.replace(" ", "_").replace("\\", "/")

        size = os.path.getsize(file_path)
        message = "file {0} {1} {2} {3}\n".format(
            file_id,
            self.options['l'],
            size,
            display_name
        )
        print("sending --> {}".format(message).encode())
        s.send(message.encode())
        with open(file_path, "rb") as f:
            print("sending -->", "<contents of" + file_path + ">")
            s.send(f.read(size))

    def send(self):
        s = socket.socket()
        s.connect((self.server, self.port))


        print("sending --> moss {}\n".format(self.user_id).encode())
        s.send("moss {}\n".format(self.user_id).encode())
        print("sending --> directory {}\n".format(self.options['d']).encode())
        s.send("directory {}\n".format(self.options['d']).encode())
        print("sending --> X {}\n".format(self.options['x']).encode())
        s.send("X {}\n".format(self.options['x']).encode())
        print("sending --> maxmatches {}\n".format(self.options['m']).encode())
        s.send("maxmatches {}\n".format(self.options['m']).encode())
        print("sending --> show {}\n".format(self.options['n']).encode())
        s.send("show {}\n".format(self.options['n']).encode())

        print("sending --> language {}\n".format(self.options['l']).encode())
        s.send("language {}\n".format(self.options['l']).encode())
        recv = s.recv(1024)
        print("recv: ", recv)
        if recv == "no":
            print("sending --> end\n")
            s.send(b"end\n")
            s.close()
            raise Exception("send() => Language not accepted by server")


        for file_path, display_name in self.base_files:
            #print("sending -->",file_path, display_name)
            self.uploadFile(s, file_path, display_name, 0)

        index = 1
        for file_path, display_name in self.files:
            #print("sending -->",file_path, display_name, index )
            self.uploadFile(s, file_path, display_name, index)
            index += 1

        print("sending --> query 0 {}\n".format(self.options['c']).encode())
        s.send("query 0 {}\n".format(self.options['c']).encode())

        response = s.recv(1024)

        print("sending --> {}\n".format("end\n").encode())
        s.send(b"end\n")
        s.close()

        print('*** response=', response)

        return response.decode().replace("\n","")

    def saveWebPage(self, url, path):
        if len(url) == 0:
            raise Exception("Empty url supplied")

        response = urlopen(url)
        content = response.read()

        f = open(path, 'w')
        f.write(content.decode())
        f.close()

*/



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


