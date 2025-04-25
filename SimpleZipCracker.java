import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import java.io.*;

public class SimpleZipCracker {
    static final String ZIP_FILE = "protected5.zip";//orginal zip file to crack password
    static final int numThreads = 6;//number thread i use
    static volatile boolean found = false;//flag if password found
    static String correctPassword = null;//stores found password

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();//start time
        //creat and start treads
        Thread[] threads = new Thread[numThreads];
        int lettersPerThread = 26 / numThreads;//divide 26 letters among thread

        for (int i = 0; i < numThreads; i++) {
            //assign a range of starting letters to each thread
            char startChar = (char) ('a' + i * lettersPerThread);
            char endChar = (i == numThreads - 1) ? 'z' : (char) (startChar + lettersPerThread - 1);//created with help from deepseek
            threads[i] = new Thread(new CrackTask(startChar, endChar, i));//create thread
            threads[i].start();//start thread
        }
        //wait for all threads
        for (Thread t : threads) {
            t.join();
        }

        long endTime = System.currentTimeMillis();//stop timer
        //print results
        if (found) {
            System.out.println("Password found: " + correctPassword);
        } else {
            System.out.println("Password not found.");
        }

        System.out.println("Total time: " + (endTime - startTime) + " ms");
    }
    /**
    * this class runs inside each thread to try different password combinations
    */
    static class CrackTask implements Runnable {
        char startChar, endChar;//character range for this thread
        int threadId;//thread number

        CrackTask(char start, char end, int id) {
            this.startChar = start;
            this.endChar = end;
            this.threadId = id;
        }

        public void run() {
            String zipCopy = "copy" + threadId + ".zip";//copy of zip
            String outputDir = "output" + threadId;//folder to ectract zip contents

            //make a copy of the zip file for this thread//Took help form AI
            if (!copyZipFile(ZIP_FILE, zipCopy)) {
                System.out.println("Thread " + threadId + ": failed to copy zip file.");
                return;
            }

            //try all possible password starting with letters in this thread range
            for (char c = startChar; c <= endChar; c++) {
                tryPasswordRecursive("" + c, 5, zipCopy, outputDir);
                if (found) break;
            }

             //Clean up created files and folders
            //Took help form AI and TA also guide me in this part
            deleteFile(zipCopy);//created with chatgpt
            deleteDirectory(new File(outputDir));//created with chatgpt
        }

        /**
         * this method tries all 5 letter lowercase password starting with given prefix
         * @param prefix current guess (e.g. a, ab, so on)
         * @param length final password length is always 5
         * @param zipCopy thread's copy zip file
         * @param outputDir folder to extract files 
         */
        //recursion was created with chatgpt
        void tryPasswordRecursive(String prefix, int length, String zipCopy, String outputDir) {
            if (found) return;//if found it then stop

            if (prefix.length() == length) {
                //if password is 5 character then try it
                try {
                    ZipFile zipFile = new ZipFile(zipCopy);
                    zipFile.setPassword(prefix);//try this password
                    zipFile.extractAll(outputDir);//try to unzip 
                   //if no error then password is correct
                    found = true;
                    correctPassword = prefix;
                    System.out.println("Thread " + threadId + " found password: " + prefix);
                } catch (ZipException e) {
                    // incorrect password, do nothing
                }
                return;
            }
            //addd next character and try again
            for (char c = 'a'; c <= 'z'; c++) {
                if (found) return;
                tryPasswordRecursive(prefix + c, length, zipCopy, outputDir);
            }
        }
        /**
         * make a copy of the zip files for the thread
         * @param src orginal zip file name
         * @param dest new file name to creat
         * @return true if successful
         */
        
        boolean copyZipFile(String src, String dest) {/*creted with help from chatgpt */
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
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        /**
         * delete single file
         * @param filename name of file to delelte
         */

        void deleteFile(String filename) {//creted with chatgpt
            File file = new File(filename);
            if (file.exists()) file.delete();
        }
        /**
         * deletes folder and all files inside it
         * @param dir the folder to delete
         */

        void deleteDirectory(File dir) {//created with chatgpt te whole method
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
                dir.delete();// delete main folder
            }
        }
    }
}