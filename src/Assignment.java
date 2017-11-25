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

class InterrogationTypeMismatch extends IllegalArgumentException {
    public InterrogationTypeMismatch()                      { super(    ); }
    public InterrogationTypeMismatch(String m)              { super(m   ); }
    public InterrogationTypeMismatch(Exception e)           { super(e   ); }
    public InterrogationTypeMismatch(String m, Exception e) { super(m, e); }
}

class Interrogator<T> {
    private String interrogation;
    private T storedValue;

    public Interrogator(String interrogation) {
        this.interrogation = interrogation;
        this.storedValue = null;
    }

    public String getInterrogation() {
        return (interrogation);
    }

    @SuppressWarnings("Unchecked")
    public void setValue(Object value) {
        try {
            this.storedValue = (T) value;
        } catch (ClassCastException e) {
            throw new InterrogationTypeMismatch("Invalid type for interrogator", e);
        }
    }

    public T getValue() {
        return (storedValue);
    }
}

class InputHandler {
    public static int menuOption(String prompt) throws InvalidMenuSelection {
        String rawInput;        /* Raw input, as provided by readEntry method */
        int processedInput;     /* Raw input converted to an integer */

        rawInput = readEntry(prompt);
        try {
            processedInput = Integer.parseInt(rawInput);
        } catch (NumberFormatException e) {
            throw new InvalidMenuSelection(rawInput + " is not a valid menu selection.");
        }

        return (processedInput);
    }

    public static ArrayList<Interrogator<Object>> conductInterrogation(ArrayList<Interrogator<Object>> inputParameters) {
        return (null);
    }

    public static String getCredential(String prompt) {
        return (readEntry(prompt));
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
        String user;
        String passwrd;
        Connection conn;

        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        }
        catch (ClassNotFoundException x)
        {
            System.out.println ("Driver could not be loaded");
        }

        user = InputHandler.getCredential("Enter database account:");
        passwrd = InputHandler.getCredential("Enter a password:");
        try
        {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@daisy.warwick.ac.uk:1521:daisy",user,passwrd);
            return conn;
        }
        catch(SQLException e)
        {
            System.out.println("Error retrieving connection");
            return null;
        }
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
                case 1:     System.out.println("Option 1");
                            break;
                case 2:     System.out.println("Option 2");
                            break;
                case 9:     loop = false;
                            break;
                default:    System.out.println("Invalid selection.");
            }
        }
    }
}