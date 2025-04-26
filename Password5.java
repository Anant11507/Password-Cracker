//three thread takes 28183 ms
//four thread takes 3567131 ms
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import java.io.*;
import java.util.Scanner;

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
        for (int i = 0; i < thcount; i++) {//start each thread to guesss letter from a to z
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
                }
        /**
         * try all 5 letter combinations using loop
         * @param firstChar start character
         * @param zipPath path to zip file
         * @param outDir where to extract file
         */
        void tryPasswords(String firstChar, String zipPath, String outDir) {
            if (found) return;
        //use 5 nastedloops for each password
            for (char c1 = firstChar.charAt(0); c1 <= 'z' && !found; c1++) {//first character
            for (char c2 = 'a'; c2 <= 'z' && !found; c2++) {//second character
            for (char c3 = 'a'; c3 <= 'z' && !found; c3++) {//third character
            for (char c4 = 'a'; c4 <= 'z' && !found; c4++) {//fourth character
            for (char c5 = 'a'; c5 <= 'z' && !found; c5++) {//fifth character
                String guess = "" + c1 + c2 + c3 + c4 + c5;//passsword attempt
                
            try {
                ZipFile zfile = new ZipFile(zipPath);
                zfile.setPassword(guess);//try current passeord
                zfile.extractAll(outDir);//when ther is no error then it is correct pasword
                found = true;//stop other therad
                correctpassword = guess;//save password
                return;//exxit
                } catch (ZipException e) {//catch wrong password and keep trying
                    // incorrect password so keep trying
                }
            }}}}}
        }
        /**
         * this method copies file to new location
         * @param tofile orginal file path
         * @param fromf copied file path 
         */
        void copyFile(String tofile, String fromf) {//took help from chatgpt for line 102-109
            try {
                // Open input and output streams
                FileInputStream fis = new FileInputStream(tofile);//open zip file
                FileOutputStream fos = new FileOutputStream(fromf);//creae new file 
                int byteread;//temporary storage 
                while ((byteread = fis.read()) != -1) {//read and write each byte
                    fos.write(byteread);//write evey byte to new copy
                }
                
                // Close file
                fis.close();
                fos.close();
                
            } catch (IOException e) {
                System.out.println("Error copy file");//errror message
            }
        }
        /*
         * this method deletes single file 
         */
        void deleteFile(String name) {//took help from chatgpt
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
