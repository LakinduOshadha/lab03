package collections.SortStrings;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

/**
 * This sorts an string array by length, shortest string first.
 * If a subset of input strings has the same length, it sorts
 * the Strings in Alphabetical order
 *
 * @author Lakindu Oshadha (lakinduoshadha98@gmail.com)
 */
public class SortStrings {
    // Global variables
    private ArrayList<String> strArr;


     /**
     * Constructor for SortStrings
     */
     public SortStrings() {
         strArr = null;
     }

     /**
      * Takes input data from user
      */
     public void getUserInput() {
         // Giving brief intro to user
         Scanner sc = new Scanner(System.in);
         System.out.println("This program reads Strings and outputs them sorted, " +
                    "by length, shortest string first. If a subset of input strings has " +
                    "the same length, it sorts the Strings in Alphabetical order");

         // Getting String srtArr
         System.out.print("No. of Strings which is to be sorted (N): ");
         int n = sc.nextInt();
         strArr = new ArrayList<>(n);

         for (int i = 0; i < n; i++) {
             System.out.print("Enter String " + (i + 1) + " : ");
             strArr.add(sc.next());
         }

     }

    /**
     * This sorts the string array by length, shortest string first.
     * If a subset of input strings has the same length, it sorts
     * the Strings in Alphabetical order
     *
     */
    private void performSortStrings(){
         Collections.sort(strArr, Comparator.comparing(String :: length).thenComparing(String::compareTo));
    }

    /**
     * Sorts and prints the String array
     *
     */
    public void printSortedStrings() {
        performSortStrings();
        System.out.println("Sorted Strings : " + strArr);
    }

    /**
     * Sorts and Returns an array containing all of the elements in 'SortStrings'
     *
     * @return an array containing all sorted strings
     */
    public String[] toArray(String x[]) {
        performSortStrings();
        return strArr.toArray(x);
    }


}
