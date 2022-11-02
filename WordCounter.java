
import java.util.*;
import java.io.*;

public class WordCounter {

    private static FileInputStream getFileReader(String filename) {
        FileInputStream fis = null;
        boolean fileExists = true;
        try {
            fis = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            fileExists = false;
        }

        return fis;
    }

    private static int getFileLength(String filename) {
        File file = new File(filename);
        int length = (int) file.length();

        return length;
    }

    private static Map<String, Integer> getOccurrences(String filename) {
        Map<String, Integer> occurrences = new TreeMap<String, Integer>();

        String delimiter_regexp = "[^a-zA-Z]+";
        FileInputStream fis = getFileReader(filename);


        Scanner fileScan = new Scanner(fis).useDelimiter(delimiter_regexp);

        String word;
        while(fileScan.hasNext()){
            word = fileScan.next();
            word = word.toLowerCase();

            Integer oldCount = occurrences.get(word);
            if ( oldCount == null ) {
                oldCount = 0;
            }
            occurrences.put(word, oldCount + 1);
        }

        fileScan.close();
        return occurrences;
    }

    private static void printMap(Map<String, Integer> occurrences) {
        int num_values = occurrences.size();

        System.out.println("There are " + num_values + " unique words in the document \n");

        for (String key: occurrences.keySet()) {
            String word = key.toString();
            String times = occurrences.get(key).toString();

            System.out.println(word + ": " + times);
        }
    }

    private static void printFileStats(String filename) {
        int length = getFileLength(filename);
        Map<String, Integer> occurrences = getOccurrences(filename);

        System.out.println("The file has length: " + length + " bytes");
        printMap(occurrences);
    }

    public static void main(String args[]) throws Exception {
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        boolean repeatFlag;

        do {
            System.out.print("Enter a file name: ");
            String filename = inFromUser.readLine();

            if (filename.length() > 0) {
                printFileStats(filename);
                repeatFlag = true;
            } else {
                repeatFlag = false;
            }
        } while(repeatFlag == true);
    }
}