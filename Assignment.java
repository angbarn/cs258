import java.io.*;
import java.sql.*;

class Assignment {
    public static int choiceCount = 8;

    public enum LoopState {
        INITIAL, NORMAL, ERROR, EXIT
    }

    private static String readEntry(String prompt) {
        try {
            StringBuffer buffer = new StringBuffer();
            System.out.print(prompt);
            System.out.flush();
            int c = System.in.read();
            while(c != '\n' && c != -1) {
                buffer.append((char)c);
                c = System.in.read();
            }
            return buffer.toString().trim();
        } catch (IOException e) {
            return "";
        }
    }

    /**
    * @param conn An open database connection 
    * @param productIDs An array of productIDs associated with an order
    * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
    * @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
    * @param staffID The id of the staff member who sold the order
    */
    public static void option1(Connection conn, int[] productIDs, int[] quantities, String orderDate, int staffID) {
        // Incomplete - Code for option 1 goes here
        System.out.println("Option 1");
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
    public static void option2(Connection conn, int[] productIDs, int[] quantities, String orderDate, String collectionDate, String fName, String lName, int staffID) {
        // Incomplete - Code for option 2 goes here
        System.out.println("Option 2");
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
    public static void option3(Connection conn, int[] productIDs, int[] quantities, String orderDate, String deliveryDate, String fName, String LName, String house, String street, String city, int staffID) {
        System.out.println("Option 3");
        // Incomplete - Code for option 3 goes here
    }

    /**
    * @param conn An open database connection 
    */
    public static void option4(Connection conn) {
        // Incomplete - Code for option 4 goes here
        System.out.println("Option 4");
    }

    /**
    * @param conn An open database connection 
    * @param date The target date to test collection deliveries against
    */
    public static void option5(Connection conn, String date) {
        // Incomplete - Code for option 5 goes here
        System.out.println("Option 5");
    }

    /**
    * @param conn An open database connection 
    */
    public static void option6(Connection conn) {
        // Incomplete - Code for option 6 goes here
        System.out.println("Option 6");
    }

    /**
    * @param conn An open database connection 
    */
    public static void option7(Connection conn) {
        // Incomplete - Code for option 7 goes here
        System.out.println("Option 7");
    }

    /**
    * @param conn An open database connection 
    * @param year The target year we match employee and product sales against
    */
    public static void option8(Connection conn, int year) {
        // Incomplete - Code for option 8 goes here
        System.out.println("Option 8");
    }

    public int[] getIntegerArray(String initialPrompt, String backupPrompt, String invalidPrompt) {
        boolean loop = true;
        boolean initial = true;
        boolean invalid = false;

        while (loop) {
            if (initial) {
                System.out.println(initialPrompt);
            } else if (invalid) {
                System.
            } else {

            }
        }
    }

    public static Connection getConnection() {
        String user;
        String passwrd;
        Connection conn;

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        }
        catch (ClassNotFoundException x) {
            System.out.println ("Driver could not be loaded");
        }

        user = readEntry("Enter database account:");
        passwrd = readEntry("Enter a password:");
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@daisy.warwick.ac.uk:1521:daisy",user,passwrd);
            return conn;
        } catch(SQLException e) {
            System.out.println("Error retrieving connection");
            return null;
        }
    }

    public static void main(String args[]) throws SQLException, IOException {
        // You should only need to fetch the connection details once
        Connection conn = getConnection();

        // Program runs while true
        LoopState programExecution = INITIAL;

        // Incomplete
        // Code to present a looping menu, read in input data and call the appropriate option menu goes here
        // You may use readEntry to retrieve input data
        while (programExecution != EXIT) {
            String rawSelection;
            int selection;

            switch (programExecution) {
                case INITIAL:  System.out.println("Welcome to the database program.");
                case  NORMAL:  rawSelection = readEntry("Please make a selection >>");
                               break;
                case   ERROR:  System.out.println("Invalid selection.");
                               rawSelection = readEntry("Please make a valid selection >>");
                               break;
            }

            try {
                // Could be performed by (char) - 'a', but this would limit the menu to 9 selections
                selection = Integer.parseInt(rawSelection);
                programExecution = NORMAL;
            } catch (NumberFormatException e) {
                selection = -1;
                programExecution = ERROR;
            }

            if (programExecution == NORMAL) {

                switch (selection) {
                    case  1:  option1(conn, [], [], "", -1);
                              break;
                    case  2:  option2(conn, []. []. "", "", "", "", -1);
                              break;
                    case  3:  option3();
                              break;
                    case  4:  option4();
                              break;
                    case  5:  option5();
                              break;
                    case  6:  option6();
                              break;
                    case  7:  option7();
                              break;
                    case  8:  option8();
                              break;
                    case  0:  programExecution = EXIT;
                              break;
                    default:  programExecution = ERROR:
                }
            }
            
        }
        

        conn.close();
    }
}