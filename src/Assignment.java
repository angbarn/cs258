import jdk.internal.util.xml.impl.Input;
import jdk.nashorn.internal.ir.annotations.Ignore;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;

class InvalidMenuSelection extends IllegalStateException {
    public InvalidMenuSelection()                      { super(    ); }
    public InvalidMenuSelection(String m)              { super(m   ); }
    public InvalidMenuSelection(Exception e)           { super(e   ); }
    public InvalidMenuSelection(String m, Exception e) { super(m, e); }
}

class ClientValidationError extends IllegalStateException {
    public ClientValidationError()                      { super(    ); }
    public ClientValidationError(String m)              { super(m   ); }
    public ClientValidationError(Exception e)           { super(e   ); }
    public ClientValidationError(String m, Exception e) { super(m, e); }
}



/**
 *
 */
class InputHandler {
    private static final String questionBoundary = " >> ";
    public static int menuOption(String prompt) throws InvalidMenuSelection {
        String rawInput;        // Raw input, as provided by readEntry method
        int processedInput;     // Raw input converted to an integer

        rawInput = readEntry(prompt);
        try {
            processedInput = Integer.parseInt(rawInput);
        } catch (NumberFormatException e) {
            throw new InvalidMenuSelection(rawInput + " is not a valid menu selection.");
        }

        return (processedInput);
    }

    private static String interrogate(String question, ValidationService.IValidator validator)
            throws ClientValidationError {
        String ans;

        ans = readEntry(question + questionBoundary);
        if (validator.validate(ans)) {
            return (ans);
        } else {
            throw new ClientValidationError("Answer to \"" + question + "\" failed to validate.");
        }
    }

    private static String[] massInterrogate(String question, ValidationService.IValidator validator)
            throws ClientValidationError {
        boolean loop;
        ArrayList<String> answers;
        String answerCurrent;
        String[] answersFinal;

        loop = true;

        answers = new ArrayList<>();
        System.out.println(question);
        while (loop) {
            answerCurrent = readEntry(questionBoundary);
            if (answerCurrent.equals("")) {
                loop = false;
            } else if (validator.validate(answerCurrent)) {
                answers.add(answerCurrent);
            } else {
                throw new ClientValidationError("Answer to \"" + question + "\" failed to validate.");
            }
        }

        answersFinal = new String[answers.size()];
        for (int i = 0; i < answers.size(); i++) {
            answersFinal[i] = answers.get(i);
        }

        return (answersFinal);
    }
    
    private static int interrogateNumeric(String question, ValidationService.NumericValidator validator)
            throws ClientValidationError {
        return (Integer.parseInt(interrogate(question, validator)));
    }
    
    private static int[] massInterrogateNumeric(String question, ValidationService.NumericValidator validator)
            throws ClientValidationError {
        return (parseIntMass(massInterrogate(question, validator)));
    }

    private static int[] parseIntMass(String[] stringArray) {
        int[] newInts = new int[stringArray.length];

        for (int i = 0; i < stringArray.length; i++) {
            newInts[i] = Integer.parseInt(stringArray[i]);
        }

        return (newInts);
    }

    public static String[] getCredentials() throws ClientValidationError {
        String[] answers;
        answers = new String[2];

        answers[0] = interrogate("Please enter your username.", new ValidationService.StringValidator());
        answers[1] = interrogate("Please enter your password.", new ValidationService.StringValidator());

        return (answers);
    }

    private static String readEntry(String prompt) {
        try
        {
            StringBuffer buffer = new StringBuffer();
            System.out.print(prompt);
            System.out.flush();
            int c = System.in.read();
            while(c != '\n' && c != -1) {
                buffer.append((char)c);
                c = System.in.read();
            }
            return buffer.toString().trim();
        }
        catch (IOException e)
        {
            return "";
        }
    }

    private static class ValidationService {
        interface IValidator {
            boolean validate(String ans);
        }

        public static class StringValidator implements IValidator {
            public boolean validate(String ans) { return (ans.length() > 0); }
        }

        public static class NumericValidator implements IValidator {
            public boolean validate(String ans) {
                return (ans.matches("\\d+(\\.\\d+)?"));
            }
        }

        public static class DateValidator implements IValidator {
            public boolean validate(String ans) {
                boolean success;
                String regexMatch;

                success = false;
                // 2 digits, a dash, a capital, two lowercase, a dash, 2 digits
                regexMatch = "\\d\\d-[A-Z][a-z]{2}-\\d\\d";

                // 012345678
                // 01-Jan-17
                if (ans.matches(regexMatch)) {
                    int day = Integer.parseInt(ans.substring(0, 2));
                    int year = Integer.parseInt(ans.substring(7, 9));

                    if (day >= 1 && day <= 31 && year >= 1900) {
                        success = true;
                    }
                }

                return (success);
            }
        }
    }

    public static class OptionHandler {
        public static void processOption1(Connection conn) {
            int[]  productIDs;
            int[]  quantities;
            String orderDate;
            int    staffID;
            ValidationService.NumericValidator numVal;
            ValidationService.DateValidator datVal;
            
            numVal = new ValidationService.NumericValidator();
            datVal = new ValidationService.DateValidator();

            productIDs = InputHandler.massInterrogateNumeric("Enter product IDs", numVal);
            quantities = InputHandler.massInterrogateNumeric("Enter quantities", numVal);
            orderDate  = InputHandler.interrogate("Enter date of order", datVal);
            staffID    = InputHandler.interrogateNumeric("Enter staff ID", numVal);

            Assignment.option1(conn, productIDs, quantities, orderDate, staffID);
        }
    }
}

class Assignment {


    /**
     * @param conn An open database connection
     * @param productIDs An array of productIDs associated with an order
     * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
     * @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
     * @param staffID The id of the staff member who sold the order
     */
    public static void option1(Connection conn, int[] productIDs, int[] quantities, String orderDate, int staffID)
    {
        // Incomplete - Code for option 1 goes here
    }

    /**
     * @param conn An open database connection
     * @param productIDs An array of productIDs associated with an order
     * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
     * @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
     * @param collectionDate A string in the form of 'DD-Mon-YY' that represents the date the order will be collected
     * @param fName The first name of the customer who will collect the order
     * @param LName The last name of the customer who will collect the order
     * @param staffID The id of the staff member who sold the order
     */
    public static void option2(Connection conn, int[] productIDs, int[] quantities, String orderDate, String collectionDate, String fName, String LName, int staffID)
    {
        // Incomplete - Code for option 2 goes here
    }

    /**
     * @param conn An open database connection
     * @param productIDs An array of productIDs associated with an order
     * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
     * @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
     * @param deliveryDate A string in the form of 'DD-Mon-YY' that represents the date the order will be delivered
     * @param fName The first name of the customer who will receive the order
     * @param LName The last name of the customer who will receive the order
     * @param house The house name or number of the delivery address
     * @param street The street name of the delivery address
     * @param city The city name of the delivery address
     * @param staffID The id of the staff member who sold the order
     */
    public static void option3(Connection conn, int[] productIDs, int[] quantities, String orderDate, String deliveryDate, String fName, String LName,
                               String house, String street, String city, int staffID)
    {
        // Incomplete - Code for option 3 goes here
    }

    /**
     * @param conn An open database connection
     */
    public static void option4(Connection conn)
    {
        // Incomplete - Code for option 4 goes here
    }

    /**
     * @param conn An open database connection
     * @param date The target date to test collection deliveries against
     */
    public static void option5(Connection conn, String date)
    {
        // Incomplete - Code for option 5 goes here
    }

    /**
     * @param conn An open database connection
     */
    public static void option6(Connection conn)
    {
        // Incomplete - Code for option 6 goes here
    }

    /**
     * @param conn An open database connection
     */
    public static void option7(Connection conn)
    {
        // Incomplete - Code for option 7 goes here
    }

    /**
     * @param conn An open database connection
     * @param year The target year we match employee and product sales against
     */
    public static void option8(Connection conn, int year)
    {
        // Incomplete - Code for option 8 goes here
    }

    public static Connection getConnection()
    {
        String[] credentials;
        Connection conn;

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException x) {
            System.out.println ("Driver could not be loaded");
        }

        credentials = InputHandler.getCredentials();
        conn = null;
        try {
            conn = DriverManager.getConnection(
                    "jdbc:oracle:thin:@daisy.warwick.ac.uk:1521:daisy",
                    credentials[0],
                    credentials[1]
            );
        } catch (ClientValidationError e) {
            System.out.println("Error entering credentials.");
        } catch(SQLException e) {
            System.out.println("Error retrieving connection.");
        }

        return (conn);
    }

    public static void exit(Connection conn) throws SQLException {
        conn.close();

        System.out.println("Database system exited.");
    }

    public static void main(String args[]) throws SQLException, IOException
    {
        // You should only need to fetch the connection details once
        Connection conn = getConnection();
        boolean loop = true;
        int input;

        while (loop) {
            try {
                input = InputHandler.menuOption("Please make a selection >> ");
            } catch (InvalidMenuSelection e) {
                System.out.println("Please enter a valid selection.\n\n");
                input = -1;
            }

            switch (input) {
                case 1:
                            InputHandler.OptionHandler.processOption1(conn);
                            break;
                case 2:     System.out.println("Option 2");
                            break;
                case 9:     loop = false;
                            System.out.println("Goodbye.");
                            break;
                default:    System.out.println("Invalid selection.");
            }
        }
    }
}