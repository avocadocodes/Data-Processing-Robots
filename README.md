# Data-Processing-Robots

## Jason Installation

1. Install link: [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
2. Install link: [Visual Sudio Code](https://code.visualstudio.com/download)
3. [Windows] Install a terminal & shell tool like link:[GitBash](https://gitforwindows.org/)
4. Install Jason:
+
### Unix and Windows::
+
Download a Jason release from link:https://github.com/jason-lang/jason/releases[here] (download the file named `jason-bin-.....zip`) and decompress it. The zip file contains documentation, examples, and a sub-directory `bin` with the file *`jason`*. It is a unix executable file, if not, change its properties with `chmod +x jason`. Finally, link:https://www.computerhope.com/issues/ch000549.htm[adds the directory `bin` in your machine `PATH`] so that the command `jason` can be executed in a terminal. 

### Linux:
You can use `apt-get` to install Jason (details link:https://github.com/chon-group/dpkg-jason[here]):
+
```
echo "deb [trusted=yes] http://packages.chon.group/ chonos main" | sudo tee /etc/apt/sources.list.d/chonos.list 
sudo apt update
sudo apt install jason-cli
```

To test the installation, run the command `jason --version` in a terminal. The output should be as follows:
