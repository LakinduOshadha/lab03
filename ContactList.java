import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This is a Phone Contact List command line application which takes input from the command window.
 *
 * <p> The first input is always the name of a ".txt" file used to store the phone records.
 * path should be the absolute path</>
 *
 * <p> If the input followed by the file name is a multi-word name,
 * it prints the corresponding mobile number(s) in the following format
 * (if there are more than one mobile number, it prints all) </>
 *
 * <code>
 * Mary Anne            808-779-1466
 * Mary Anne David      843-798-6698
 * Mary Anne Doe        801-557-2819
 * </code>
 *
 * <p> If the input followed by the file name is a number(only digits),
 * it prints the corresponding name. (If there are duplicate numbers, it throws an exception) </>
 *
 * <p> If the following inputs is a name followed by a number, it adds to the contacts list
 * in the alphabetical order of names. If the contacts list does not exit, the text file and necessary
 * directories are created. NO duplicates are allowed. If there is, it throws an Exception</>
 *
 * <p> It will add contact details from a json file to the contact_list file when the input arguments are in
 * C\Downloads\contact_list.txt add_json C:\example.json format </p>
 *
 * @author Lakindu Oshadha (lakinduoshadha98@gmail.com)
 */
public class ContactList {
    // Initializing contactList as global variable
    private static File contactList;

    /**
     * <tt>printLineByName(name)</tt> method searches and prints the contact details
     * when the name is given.
     *
     * @param name name
     * @throws FileNotFoundException If there are no file in the path
     */
    private static void printLineByName(String name) throws FileNotFoundException {
        Scanner in = new Scanner(contactList);
        boolean flag = false;   // to print a msg if there is no such name

        // Prints the details of relevant names
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (line.contains(name)) {
                printLine(line);
                flag = true;
            }
        }
        if (!flag) System.out.println("ERROR : No such Contact found.!");
        in.close();
        return;
    }

    /**
     * <tt>printLineByNumber(number)</tt> method searches and prints the contact details
     *  when the number is given.
     *
     * @param number phone number
     * @throws FileNotFoundException If there are no file in the path
     * @throws StoredDataDuplicateNumberException  If multiple names are found
     */
    private static void printLineByNumber(String number) throws FileNotFoundException {

        Scanner in = new Scanner(contactList);
        boolean flag = false; // to detect duplicates
        String tempLine = null; // to store the line before printing

        // Searches the details of relevant names
        try {
            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (getNumber(line).contains(number)) {
                    if (flag)
                        throw new StoredDataDuplicateNumberException("Duplicate Numbers Found in the Contact List.!");
                    tempLine = line;
                    flag = true;
                    }
                }
        } catch(StoredDataDuplicateNumberException e) {
            System.out.println(e.toString());
        }
        // Printing the an Error msg
        if (!flag) {
            System.out.println("No such Number found.!");
        } else {
            System.out.println(tempLine);
        }
        in.close();
        return;
    }

    /**
     * <tt>insertEntry(name, number)</tt> inserts an entry to the contact list inn the corresponding directory.
     *
     * @param name name
     * @param number phone number
     * @throws FileNotFoundException If there are no file in the path
     * @throws StoredDataDuplicateNumberException  If the phone number is already available in the .txt file.
     */
    private static void insertEntry(String name, String number) throws FileNotFoundException,
            InputDuplicateNumberException {
        Scanner sc = new Scanner(contactList);
        // Creating new string with input arguments
        String newNumber = number.substring(0,3) + "-" + number.substring(3,6) + "-" + number.substring(6);
        String newLine = name + ": " + newNumber;
        // Creating an arrayList to add the new entry and sort
        ArrayList<String> linesArr = new ArrayList<>();

        // Adding entries to the created arrayList
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            // If the number already in the list, <tt>throwing the
            // InputDuplicateNumberException</tt> exception
            if (line.contains(newNumber)) {
                throw new InputDuplicateNumberException("The Number is Already Available in the " +
                        "Contact List : " + line);
            }
            linesArr.add(line);
        }
        // Adding new entry to the ArrayList
        linesArr.add(newLine);
        Collections.sort(linesArr); // Sorting the array in ascending order

        // Creating the PrintWriter class object and clearing the available data on it
        PrintWriter out = new PrintWriter(contactList.getAbsolutePath());

        // Printing the sorted array into the text file
        for (int i = 0; i < linesArr.size(); i++) {
            out.println(linesArr.get(i));
        }
        out.close();
        System.out.println("Successfully added the contact details to the Contact List.!");

    }

    /**
     * <tt>addDetailsFromJson</tt> inserts contact details form a json file to a text file
     * @param jsonFile jason file location
     * @throws IllegalArgumentException when input is not in given format
     */
    private static void addDetailsFromJson(String jsonFile) throws IllegalArgumentException {
        // Getting details from Json file assigning them to a JSON Array
        JSONParser parser = new JSONParser();
        FileReader reader = null;
        Object obj = null;
        try {
            reader = new FileReader(jsonFile);
            obj = parser.parse(reader);
        } catch (Exception e) {
            throw new IllegalArgumentException("Input Invalid. Please input Json file path as <C:\\example.json>");
        }
        JSONArray arrJson = (JSONArray)obj;

        // Inserting Details to Text file
        arrJson.forEach(el -> {
            JSONObject jObj = (JSONObject)el;
            try {
                insertEntry((String)jObj.get("name"), (String)jObj.get("number"));
            } catch (FileNotFoundException | InputDuplicateNumberException e) {
                System.out.println(e.toString());
            }
        });
    }

    /**
     * <tt>getNumber(line)</tt> Returns the phone number with pure digits of a given specific line
     * This removes dashes in the phone number
     *
     * @param line row of the txt file
     * @return the phone number with pure digits
     */
    private static String getNumber(String line) {
        int i = 0;
        ArrayList<String> numArr = new ArrayList<>();

        // Finding the number and copy it to another string
        while (line.charAt(i) != ':')  i++;
        String numberWithDashes = line.substring(i + 2);

        // Adding the numbers to the ArrayLists. This removes dashes
        for (i = 0; i < numberWithDashes.length(); i++)  {
            if (Character.isDigit(numberWithDashes.charAt(i))) numArr.add(Character
                    .toString(numberWithDashes.charAt(i)));
        }
        // Creating the string and returning the number as a string
        String number = String.join("", numArr);
        return number;
    }

    /**
     * <tt>printLine(line)</tt> prints a entry in the following format:
     *
     * <code>
     * Mary Anne            808-779-1466
     * Mary Anne David      843-798-6698
     * Mary Anne Doe        801-557-2819
     * </code>
     *
     * @param line the line which is to be printed
     */
    private static void printLine(String line) {
        int i = 0;
        while (line.charAt(i) != ':')  i++;
        String name = line.substring(0, i);
        String number = line.substring(i + 2);
        // Maximum name length is 30 characters, Maximum number length is 20 characters
        System.out.printf("%-30s%-20s%-1s", name, number, "\n");
        return;
    }

    /**
     * <tt>separatePath(args)</tt> separates path from the command line args
     * This method can handle the paths with spaces even.
     *
     * @param args command line args
     * @return string array which is the path in index 0
     */
    private static String[] separatePath(String[] args) throws InvalidFileNameException, InsufficientInputsException {
        // Finding .txt file
        int i = 0;
        while (i < args.length && !args[i].contains(".txt")) {
            i++;
        }
        if (i == args.length)
            throw new InvalidFileNameException("The Entered file name is not a name of a textfile");

        // Creating string with path
        String[] pathArr = new String[i + 1];
        System.arraycopy(args, 0, pathArr, 0, pathArr.length);
        String path = String.join(" " ,pathArr);

        // Creating a string array with separated path
        String[] separatedPathArr = new String[args.length - i];
        separatedPathArr[0] = path;
        System.arraycopy(args, i + 1, separatedPathArr, 1,
                separatedPathArr.length - 1);

        if (separatedPathArr.length < 2)
            throw new InsufficientInputsException("Inputs are not sufficient.!");
        return separatedPathArr;
    }

    /**
     * <tt>extractName(separatedPathArr, n)</tt> returns the name from a path separated array.
     * Every first letter of the returning name is capital
     *
     * @param separatedPathArr the path separated array
     * @param n number of words in the name
     * @return name name
     */
    private static String extractName(String[] separatedPathArr, int n) {
        String[] nameArr = new String[n];
        System.arraycopy(separatedPathArr, 1, nameArr, 0, nameArr.length);
        String name = String.join(" " , nameArr);
        return capitalizeName(name);
    }

    /**
     * <tt>capitalizeName(name)</tt> method capitalizes every first letter of a given specific name.
     *
     * @param name name
     * @return Capitalized name
     */
    private static String capitalizeName(String name) {
        char[] charArr = name.toLowerCase().toCharArray();
        boolean flag = false; // to track the spaces
        // Capitalizing the fist letter of every word
        for (int i = 0; i < charArr.length; i++) {
            if (!flag && Character.isAlphabetic(charArr[i])) {
                charArr[i] = Character.toUpperCase(charArr[i]);
                flag = true;
            } else if (Character.isWhitespace(charArr[i])) {
                flag = false;   // Next character is Capitalizing because flag == false
            }
        }
        return String.valueOf(charArr);
    }

    /**
     * <tt>separateDetails(String [] args) </tt> method separate the command line args
     * to path and name/number
     *
     * <p> If the input followed by the file name is a multi-word name,
     * this method returns an String array with <tt>[path, name, "name"]</tt>
     * This "name" in index 2 is used to find whether the input after the path is a name or a number</>
     *
     * <p> If the input followed by the file name is a number,
     * this method returns an String array with <tt>[path, phoneNumber, "number"]</tt>
     * This "number" in index 2 is used to find whether the input after the path is a name or a number</>
     *
     * <p> If the following inputs is a name followed by a number,
     * this method returns an String array with <tt>[path, name, phoneNumber]</tt></>
     *
     * <p> If the following input is <tt>add_json</tt>, the it returns [path, json_file_name, "json"]</p>
     *
     * @param args command line args
     * @return String array with length 3.
     */
    private static String[] separateDetails(String [] args) throws IllegalArgumentException, InvalidFileNameException, InsufficientInputsException {
        if (args.length == 0)
            throw new InsufficientInputsException("You entered nothing.! Use the given input format");
        String[] allSeparatedArr = new String[3];
        // separates path from the args
        String[] separatedPathArr = separatePath(args);
        String path = separatedPathArr[0];

//        System.out.println(separatedPathArr[2]);
//        System.out.println(separatedPathArr[1]);

        try {
            if (separatedPathArr[1].equals("add_json")) {
                allSeparatedArr[0] = path;
                allSeparatedArr[1] = separatedPathArr[2];
                allSeparatedArr[2] = "json";
                return allSeparatedArr;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Input Format");
        }

        // Assigning data to the returning array according to the format
        if (Character.isDigit(separatedPathArr[separatedPathArr.length - 1].charAt(0))) {
            validateINumber(separatedPathArr[separatedPathArr.length - 1]);
            if (separatedPathArr.length == 2) {
                // If the input followed by the file name is a number
                allSeparatedArr[0] = path;
                allSeparatedArr[1] = separatedPathArr[1];
                allSeparatedArr[2] = "number";

            } else {
                // If the following inputs is a name followed by a number
                String name = extractName(separatedPathArr, separatedPathArr.length - 2); // getting name
                validateIName(name);
                allSeparatedArr[0] = path;
                allSeparatedArr[1] = name;
                allSeparatedArr[2] = separatedPathArr[separatedPathArr.length - 1];
            }
        } else {
            // If the input followed by the file name is a multi-word name
            String name = extractName(separatedPathArr, separatedPathArr.length - 1); // getting name
            validateIName(name);
            allSeparatedArr[0] = path;
            allSeparatedArr[1] = name;
            allSeparatedArr[2] = "name";
        }
        return allSeparatedArr;
    }

    /**
     * <tt>validateINumber</tt> checks whether the input number is valid
     *
     * @param number number
     * @throws InvalidNumberException when number includes symbols and letters or number of digits not 10
     */
    private static void validateINumber(String number) throws InvalidNumberException {
        // To check whether the number includes symbols and letters
        Pattern pattern = Pattern.compile("[^0-9]");
        Matcher matcher = pattern.matcher(number);
        if (number.length() != 10)  // To check the number of digits
            throw new InvalidNumberException("Entered number should be 10 digits.Double check your number.!");
        if (matcher.find())
            throw new InvalidNumberException("Entered number Contains letters/symbols.Double check the number.!");
    }

    /**
     * <tt>validateIName</tt> checks whether the input name is valid
     *
     * @param name name
     * @throws InvalidNameException when name includes symbols and numbers
     */
    private static void validateIName(String name) throws InvalidNameException {
        // To check whether the name includes symbols and numbers
        Pattern pattern = Pattern.compile("[^a-zA-Z ]");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find())
            throw new InvalidNameException("Entered Name Contains numbers/symbols.Double check the name.!");
    }


    /**
     * This is the main method.
     *
     * <p> The first input is always the name of a ".txt" file used to store the phone records.
     * path should be the absolute path </>
     *
     * <p> If the input followed by the file name is a multi-word name,
     * it prints the corresponding mobile number(s) in following format (if there are more than one
     * mobile number, it prints all) </>
     *
     * <code>
     * Mary Anne            808-779-1466
     * Mary Anne David      843-798-6698
     * Mary Anne Doe        801-557-2819
     * </code>
     *
     * <p> If the input followed by the file name is a number(only digits),
     * it prints the corresponding name. (If there are duplicate numbers, it throws an exception) </>
     *
     * <p> If the following inputs is a name followed by a number, it adds to the contacts list
     * in the alphabetical order of names. If the contacts list does not exit, the text file and necessary
     * directories are created. NO duplicates are allowed. If there is, it throws an Exception</>
     *
     * <p> It will add contact details from a json file to the contact_list file when the input arguments are in
     * C\Downloads\contact_list.txt add_json C:\example.json format
     *
     *  @param args command line arguments.
     *              First provide Absolute path of the .txt file.
     *
     *              If you want to search the number/name, then provide the name name or
     *              number separated with space.
     *
     *              If you want to add a contact entry to the contact list, provide a name
     *              followed by a number after the path.
     * @throws IOException If there are no file in the path
     */
    public static void main(String[] args) throws IOException {
        try {
            String[] separatedDetails = separateDetails(args);
            contactList = new File(separatedDetails[0]);    // Creating File obj


            // Finding the number/ name or adding the contact details to the contact list
            if (separatedDetails[2] == "name") {        // If the input followed by the file name is a multi-word name
                printLineByName(separatedDetails[1]);   // Prints the contact details of the corresponding name

            } else if (separatedDetails[2] == "number") {   // If the following inputs is a name followed by a number
                printLineByNumber(separatedDetails[1]);     // Prints the contact details of the corresponding number

            } else if(separatedDetails[2] == "json") {
                addDetailsFromJson(separatedDetails[1]);
            } else {    // If the following inputs is a name followed by a number
                // Creating the directory and a .txt file if theres is no .txt file in the given directory
                if (!contactList.exists()) {
                    Path path = Paths.get(contactList.getParentFile().getAbsolutePath());
                    Files.createDirectories(path);
                    contactList.createNewFile();
                    System.out.println("File doesnt exist. Created new file");
                }
                // Inserting the contact details to the contact list
                insertEntry(separatedDetails[1], separatedDetails[2]);
            }
        } catch(Exception e) {
            System.out.println(e.toString());
        } finally {
            System.out.println("Thank you for using contact list app");
        }

    }

}

/** ----- Exceptions -----

/**
 * <tt>DuplicateNumberException</tt> extends IOException.
 * This exception is thrown If duplicate contact numbers are found.
 *
 */

class DuplicateNumberException extends IOException {
    /**
     * Constructs a <tt>DuplicateNumberException</tt> with default error detail message.
     *
     */
    public DuplicateNumberException() {}

    /**
     * Constructs a <tt>DuplicateNumberException</tt> with the
     * specified detail message.
     *
     * @param message The detailed message.
     */
    public DuplicateNumberException(String message) {
        super(message);
    }
}

/**
 * <tt>IllegalArgumentException</tt> extends IOException.
 * This exception is thrown If invalid arguments are found.
 *
 */

class IllegalArgumentException extends IOException {
    /**
     * Constructs a <tt>IllegalArgumentException</tt> with default error detail message.
     *
     */
    public IllegalArgumentException() {}

    /**
     * Constructs a <tt>IllegalArgumentException</tt> with the
     * specified detail message.
     *
     * @param message The detailed message.
     */
    public IllegalArgumentException(String message) {
        super(message);
    }
}


/**
 * <tt>MissingArgumentsException</tt> extends IOException.
 * This exception is thrown If there are missing arguments
 *
 */

class MissingArgumentsException extends IOException {
    /**
     * Constructs a <tt>MissingArgumentsException</tt> with default error detail message.
     *
     */
    public MissingArgumentsException() {}

    /**
     * Constructs a <tt>MissingArgumentsException</tt> with the
     * specified detail message.
     *
     * @param message The detailed message.
     */
    public MissingArgumentsException(String message) {
        super(message);
    }
}


/**
 * <tt>StoredDataDuplicateNumberException</tt> extends IOException.
 * This exception is thrown If duplicate numbers are found in the contact list.
 *
 */

class StoredDataDuplicateNumberException extends DuplicateNumberException {
    /**
     * Constructs a <tt>StoredDataDuplicateNumberException</tt> with default error detail message.
     *
     */
    public StoredDataDuplicateNumberException() {}

    /**
     * Constructs a <tt>StoredDataDuplicateNumberException</tt> with the
     * specified detail message.
     *
     * @param message The detailed message.
     */
    public StoredDataDuplicateNumberException(String message) {
        super(message);
    }
}


/**
 * <tt>InputDuplicateNumberException</tt> extends IOException.
 * This exception is thrown when duplicate numbers are inputted.
 *
 */

class InputDuplicateNumberException extends DuplicateNumberException {
    /**
     * Constructs a <tt>InputDuplicateNumberException</tt> with default error detail message.
     *
     */
    public InputDuplicateNumberException() {}

    /**
     * Constructs a <tt>InputDuplicateNumberException</tt> with the
     * specified detail message.
     *
     * @param message The detailed message.
     */
    public InputDuplicateNumberException(String message) {
        super(message);
    }
}

/**
 * <tt>InvalidNameException</tt> extends IOException.
 * This exception is thrown If the name includes of symbols and numbers
 *
 */

class InvalidNameException extends IllegalArgumentException {
    /**
     * Constructs a <tt>InvalidNameException</tt> with default error detail message.
     *
     */
    public InvalidNameException() {}

    /**
     * Constructs a <tt>InvalidNameException</tt> with the
     * specified detail message.
     *
     * @param message The detailed message.
     */
    public InvalidNameException(String message) {
        super(message);
    }
}

/**
 * <tt>InvalidNumberException</tt> extends IOException.
 * This exception is thrown If the number includes symbols and letters or
 * number of digits not 10
 *
 */

class InvalidNumberException extends IllegalArgumentException {
    /**
     * Constructs a <tt>InvalidNumberException</tt> with default error detail message.
     *
     */
    public InvalidNumberException() {}

    /**
     * Constructs a <tt>InvalidNumberException</tt> with the
     * specified detail message.
     *
     * @param message The detailed message.
     */
    public InvalidNumberException(String message) {
        super(message);
    }
}

/**
 * <tt>InsufficientInputsException</tt> extends IOException.
 * This exception is thrown when the app receives less than 2 arguments
 *
 */

class InsufficientInputsException extends MissingArgumentsException {
    /**
     * Constructs a <tt>InsufficientInputsException</tt> with default error detail message.
     *
     */
    public InsufficientInputsException() {}

    /**
     * Constructs a <tt>InsufficientInputsException</tt> with the
     * specified detail message.
     *
     * @param message The detailed message.
     */
    public InsufficientInputsException(String message) {
        super(message);
    }
}

/**
 * <tt>InvalidFileNameException</tt> extends IOException.
 * This exception is thrown If The first input is not a proper name of a text file
 *
 */

class InvalidFileNameException extends MissingArgumentsException {
    /**
     * Constructs a <tt>InvalidFileNameException</tt> with default error detail message.
     *
     */
    public InvalidFileNameException() {}

    /**
     * Constructs a <tt>InvalidFileNameException</tt> with the
     * specified detail message.
     *
     * @param message The detailed message.
     */
    public InvalidFileNameException(String message) {
        super(message);
    }
}



