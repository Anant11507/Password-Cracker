import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import java.io.*;
import java.util.*;

public class Password5 {
    static boolean found = false;//if found password then stop other thread
    static String correctpassword = "";//store correct password if found

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.print("Number of threads:");
        int thcount = sc.nextInt();//give how many thread to use (6 thread i use)

        long startTime = System.currentTimeMillis();//time start of password cracking

        Thread[] th = new Thread[thcount];//create arraya of thread
        int lettersPerThread = 26 / thcount;//26 letters divide to different thread
        //loop to start each thread 
        for (int i = 0; i < thcount; i++) {//start each thread to guesss letter from aaaaa to zzzzz
            char startChar = (char) ('a' + i * lettersPerThread);//starting letter for each thread
            char endChar; //ending letter for each thread
            if (i == thcount - 1) {
                endChar = 'z';
            } else {
                endChar = (char) (startChar + lettersPerThread - 1);//calculating ending letter for this thread

            }
            //make object that try password for this threaad
            PasswordCracker cracker = new PasswordCracker(startChar, endChar, i);
            th[i] = new Thread(cracker);//put that object inside thread
            th[i].start();//start thread
        }
        //wait for all threaad to finish
        for (int i = 0; i < thcount; i++) {
            th[i].join();//wait for each thread
        }

        long endTime = System.currentTimeMillis();//record the end time

        System.out.println("Password found: " + correctpassword);//show correct password after finding
        System.out.println("Total time to find password is " + (endTime - startTime) + " ms");//show how much time it takes
    }
    /**
     * this class is for each thread that tries cracking password
     */
    static class PasswordCracker implements Runnable {
        char startChar;//starting letter for this thread range
        char endChar;//ending letter for this thread range
        int threadId;//id for thread

        PasswordCracker(char start, char end, int id) {
            this.startChar = start;//set start character
            this.endChar = end;//set end character
            this.threadId = id;//set thread id
        }

        public void run() {
            String zipcopy = "copy" + threadId + ".zip";//creat copy of the file for thread
            String contentdir = "extracted_contents" + threadId;//directory extracting files

            copyFile("protected5.zip", zipcopy);//copy orgial file to this thread file
            //try password from current range assigned to this thread
            for (char c = startChar; c <= endChar; c++) {
                tryPasswords("" + c, zipcopy, contentdir);//try each letter as start of the password
                if (found) break;//stop if correct password is found
            }

            deleteFile(zipcopy);//delete this thread zip copy
            deleteFolder(new File(contentdir));//delete extracted files after use
            /**
             * this method tries different password gusses of 5 letter
             */
        }
        //took help from AI
        void tryPasswords(String guess, String zipPath, String outDir) {
            if (found) return;//stop if password is already found

            if (guess.length() == 5) {//if the password is 5 letter long then try
                try {
                    ZipFile zfile = new ZipFile(zipPath);//open zip file
                    zfile.setPassword(guess);//set guess password
                    zfile.extractAll(outDir);//try to extract files using guess
                    found = true;//if there is no error then password is correct
                    correctpassword = guess;//store correct password
                } catch (ZipException e) {//if password is wrong then error come here but i ignoe it
                    
                }
                return;//return after trying password
            }
            //if thr password is not 5 letters then add one more letter and try again
            for (char c = 'a'; c <= 'z'; c++) {
                if (found) return;
                tryPasswords(guess + c, zipPath, outDir);
            }}
         /*
         * this method copies file to new location
         */
        //Took help from chatgpt
        void copyFile(String source, String dest) {
            try {
                FileInputStream fis = new FileInputStream(source);//open source zip file
                FileOutputStream fos = new FileOutputStream(dest);//create new file for copy
                byte[] buffer = new byte[1024];//buffer for coping data
                int len;
                while ((len = fis.read(buffer)) > 0) {//read and copy file
                    fos.write(buffer, 0, len);
                }
                fis.close();
                fos.close();
            } catch (IOException e) {
                System.out.println("Error copying file");//print error message ifcoping fails
            }}
        /*
         * this method deletes single file 
         * Created from chatgpt
         */
        void deleteFile(String name) {
            File file = new File(name);
            if (file.exists()) {
                file.delete();
            }
        }
        /*
         * this method delets folder and its contents
         */
        //Took help from chatgpt
        void deleteFolder(File folder) {
            if (folder.exists()) {
            File[] files = folder.listFiles();//list all files in folder
            if (files != null) {
            for (File f : files) {
             if (f.isDirectory()) {
             deleteFolder(f);//delete recursively
            } else {
            f.delete();//if its file then delete
             }}}
                folder.delete();//delete folder itself
            }}}}