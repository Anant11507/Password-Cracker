import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import java.io.*;
import java.util.Scanner;

public class Password5 {
    static final String ZIP_FILE = "protected5.zip";//file to crack password
    static volatile boolean found = false;//flag if password found
    static String correctPassword = null;//stores found password
    static int numThreads;//i have to enter how many thread i want to use (6 thread)
    /**
     *main method to start program
     */
    public static void main(String[] args) throws Exception {
        Scanner s = new Scanner(System.in);
        System.out.print("Enter number of threads: ");
        numThreads = s.nextInt();  // ask user for thread count

        long startTime = System.currentTimeMillis();//start time
        //create and start threads
        Thread[] threads = new Thread[numThreads];
        int lettersPerThread = 26 / numThreads;//divide 26 letters in thread
        //loop through threads and assign each one a range fo letter
        for (int i = 0; i < numThreads; i++) {
            char startChar = (char) ('a' + i * lettersPerThread);
            char endChar = (i == numThreads - 1) ? 'z' : (char) (startChar + lettersPerThread - 1);//created with help of deepseek
            threads[i] = new Thread(new CrackTask(startChar, endChar, i));
            threads[i].start();//start each thread
        }
        //wait for all threads to finish
        for (Thread t : threads) {
            t.join();
        }

        long endTime = System.currentTimeMillis();//stop time to measure total time taken
        //display results
        if (found) {
            System.out.println("Password found: " + correctPassword);
        } else {
            System.out.println("Password not found.");
        }
        //show haw long the cracking take
        System.out.println("Total time: " + (endTime - startTime) + " ms");
    }
    /**
     * cracktask is the task that each threaad will run
     * it tries different passwords based on the range of letters assigned to it
     */
    static class CrackTask implements Runnable {
        char startChar, endChar;//range of letters this thread will try
        int threadId;//unique id for this thread
        /**
         * constructor to initialize the range of charactersa nd thread id
         * @param start staring letter for this thread range
         * @param end ending letter for this thread range
         * @param id unique id for the thread
         */
        CrackTask(char start, char end, int id) {
            this.startChar = start;
            this.endChar = end;
            this.threadId = id;
        }
        /**
         * this method runs the thread task which is copying zip file and trying passwords
         */
        public void run() {
            String zipCopy = "copy" + threadId + ".zip";//create a copy of zip file
            String outputDir = "output" + threadId;//folder for extrcting zip contents
            //make copy of zip file for this thread//took help from AI
            if (!copyZipFile(ZIP_FILE, zipCopy)) {
                System.out.println("Thread " + threadId + ": failed to copy zip file.");
                return;
            }
            //try passwords within range assigned to this thread
            for (char c = startChar; c <= endChar; c++) {
                tryPasswordRecursive("" + c, 5, zipCopy, outputDir);
                if (found) break;//stop if password is found
            }
            //clean created files and folders after finishing//took help from AI and TA also help me in lab 
            deleteFile(zipCopy);
            deleteDirectory(new File(outputDir));//created with chatgpt
        }
        /**
         * this method tries all 5 letter lowercasw passwords starting with the given prefix
         * it does recursively by adding one letter at a time
         * @param prefix current guess
         * @param length length of password which is 5 to be
         * @param zipCopy path to the thread zip file copy
         * @param outputDir directory for extracted file
         */
        void tryPasswordRecursive(String prefix, int length, String zipCopy, String outputDir) {
            if (found) return;//stop if password is already found

            if (prefix.length() == length) {//if password is 5 character then try it
                try {
                    ZipFile zipFile = new ZipFile(zipCopy);
                    zipFile.setPassword(prefix);////try this password
                    zipFile.extractAll(outputDir);//attempt to extract files
                    found = true;//if no error occers then password is correct
                    correctPassword = prefix;
                } catch (ZipException e) {
                    // incorrect password
                }
                return;
            }
            //add next letters and continue checking
            for (char c = 'a'; c <= 'z'; c++) {
                if (found) return;//stop if password is found
                tryPasswordRecursive(prefix + c, length, zipCopy, outputDir);
            }
        }
        /**
         * this method copies zip files to a new location for this thread
         * @param src path to orginal zip file
         * @param dest path for new copide zip file
         * @return true if successfull otherwise false
         */
        boolean copyZipFile(String src, String dest) {//created with help from chatgpt
            try {
                FileInputStream fis = new FileInputStream(src);
                FileOutputStream fos = new FileOutputStream(dest);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fis.close();
                fos.close();
                return true;//return true when file copy is successful
            } catch (IOException e) {
                return false;//return false if error occured during file copy
            }
        }
        /**
         * delete single file
         * @param filename name of file to delete
         */
        //took help form chatgpt
        void deleteFile(String filename) {
            File file = new File(filename);
            if (file.exists()) file.delete();//delete file if it exists
        }
        /**
         * deletes directory and all files inside it
         * @param dir directory to delete
         */
        void deleteDirectory(File dir) {//this method has been created help of chatgpt
            if (dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isDirectory()) {
                            deleteDirectory(f);//delete subfolder
                        } else {
                            f.delete();//delete file
                        }
                    }
                }
                dir.delete();//delete main folder
            }
        }
    }
}
