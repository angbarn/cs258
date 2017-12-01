import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.text.NumberFormat;

/**
 * Thrown when a user attempts to make an invalid menu selection.
 */
@SuppressWarnings("serial")
class InvalidMenuSelection extends IllegalStateException {
    /**
     * Throw exception with error message.
     * @param m The message to provide.
     */
    public InvalidMenuSelection(String m) { super(m ); }
}

/**
 * Thrown when Java-side validation fails. A checked exception so that we can be sure we catch it and deal with it
 * properly.
 */
@SuppressWarnings("serial")
class ClientValidationError extends Exception {
    /**
     * Throw exception with error message.
     * @param m The message to provide
     */
    public ClientValidationError(String m) { super(m ); }
}

/**
 * Handles all user input for the program.
 */
class InputHandler {
    /**
     * Printer at the end of an interrogation question. Separates the user's answer from the question.
     * Rather than include it in all questions, it's much easier to include it once and concat when it's required.
     */
    private static final String questionBoundary = " >> ";
    
    /**
     * Turns a user's raw text input into a usable integer that we can switch on later.
     * @param prompt The raw text from the user's selection input
     * @return The raw text converted to integer format
     * @throws InvalidMenuSelection If the text cannot be converted to an integer
     */
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
    
    /**
     * Gets the username and password from the user.
     * This information is returned in an array, where the first item is the username, and the second is that password.
     * @return A string array, where the first item is the username, and the second is the password
     * @throws ClientValidationError When one of the inputs is the empty string
     */
    public static String[] getCredentials() throws ClientValidationError {
        String[] answers;
        answers = new String[2];

        answers[0] = interrogate("Please enter your username.", new ValidationService.StringValidator());
        answers[1] = interrogate("Please enter your password.", new ValidationService.StringValidator());

        return (answers);
    }
    
    /**
     * Poses a question to the user, and performs validation on their input, before returning it back.
     * A higher-level interface to readEntry(...).
     * Questions automatically have {@link #questionBoundary} appended. Do not add a custom ": " or equivalent.
     * @param question The prompt to give the user. Automatically has {@link #questionBoundary} appended.
     * @param validator The {@link InputHandler:IValidator} to use to validate an input.
     * @return The string provided by the user.
     * @throws ClientValidationError When the validation is unsuccessful.
     */
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
    
    /**
     * @see #interrogate(String, ValidationService.IValidator)
     * A slight twist, in that inputs are automatically cast to integers when returned. This means that any validator
     * used must be a subclass of {@link ValidationService:NumericValidator}. Currently, the only class which fits this
     * bill is {@code NumericValidator} itself, but for the purposes of future proofing, we aren't taking out the
     * requirement to specify a validator just yet.
     * @param question The prompt to give the user. Automatically has {@link #questionBoundary} appended.
     * @param validator The {@link InputHandler:IValidator} to use to validate an input.
     * @return Whatever was provided by the user cast to an integer
     * @throws ClientValidationError When the validation is unsuccessful.
     */
    private static int interrogateNumeric(String question, ValidationService.NumericValidator validator)
            throws ClientValidationError {
        return (Integer.parseInt(interrogate(question, validator)));
    }
    
    /**
     * Reads an entry from the user.
     * @param prompt The prompt to give the user when requesting an input
     * @return Whatever the user gave us as an input
     */
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
    
    /**
     * Contains various classes which deal with Java-side validation of user input.
     * Since most of the validation we want to do is SQL-side, validators are relatively basic.
     * However, a certain amount of Java-side validation is nice for the purposes of UX.
     */
    private static class ValidationService {
        /**
         * Specifies that the class is a validator - that is, it will test a user's input to see if it conforms to the
         * required formatting guidelines.
         * A relatively powerful concept, but used fairly simply in this program.
         */
        interface IValidator {
            /**
             * Ensure that the provided user input conforms to the required formatting standards.
             * @param ans The provided user input
             * @return {@code true} if the user's input is correctly formatted. {@code false} otherwise.
             */
            boolean validate(String ans);
        }
    
        /**
         * Ensures that the user's input isn't just the empty string. A relatively general purpose validator, for all
         * those times where we don't really need a validator, but the method requires us to have one anyway.
         */
        public static class StringValidator implements IValidator {
            /**
             * Ensure length is greater than 0 (i.e., input is not the empty string)
             * @param ans The provided user input
             * @return {@code true} if the user's input is longer than "". {@code false} otherwise.
             */
            public boolean validate(String ans) { return (ans.length() > 0); }
        }
    
        /**
         * Ensures that the user's input is numeric - i.e., it's an integer.
         * The program will never need to deal with decimal numbers, so there is no need to allow for them.
         * If this were ever the case, the regex "\d+(\.\d+)?" seems to achieve this job relatively cleanly.
         * It's important that any subclass of NumericValidator results in a string for which Integer.parseInt(...)
         * succeeds.
         */
        public static class NumericValidator implements IValidator {
            /**
             * Ensures there are no non-digit characters in the string.
             * @param ans The provided user input
             * @return {@code true} if the user's input is made up of only digits. {@code false} otherwise.
             */
            public boolean validate(String ans) {
                return (ans.matches("\\d+"));
            }
        }
    
        /**
         * Ensures that the user's input matches the date format required by SQL, and that the date is "valid" in the
         * loosest sense of the word - the day is between 1 and 31.
         */
        public static class DateValidator implements IValidator {
            /**
             * Ensures that the user's input is a valid date format (DD-Mon-YY). Nothing else is acceptable for the
             * purposes of this solution, so should be rejected.
             * @param ans The provided user input
             * @return {@code true} if validation is successful. {@code false} otherwise.
             */
            public boolean validate(String ans) {
                boolean success;
                String regexMatch;

                success = false;
                // 2 digits, a dash, a capital, two lowercase, a dash, 2 digits
                regexMatch = "\\d\\d-[A-Z][a-z]{2}-\\d\\d";
                
                if (ans.matches(regexMatch)) {
                    // 012345678
                    // 01-Jan-17
                    int day = Integer.parseInt(ans.substring(0, 2));
                    int year = Integer.parseInt(ans.substring(7, 9));

                    if (day >= 1 && day <= 31) {
                        success = true;
                    }
                }

                return (success);
            }
        }
    }
    
    /**
     * Handles fetching the required user inputs for a given menu item, performs some light processing to package up
     * inputs correctly, and then passes these to the resulting methods.
     * This allows the menu switch statement to be much cleaner.
     */
    public static class OptionHandler {
        /**
         * A validator to ensure inputs are strings
         */
        private static ValidationService.StringValidator strVal;
        /**
         * A validator to ensure inputs will cast to integers successfully.
         */
        private static ValidationService.NumericValidator numVal;
        /**
         * A validator to ensure an input is a correctly formatted date.
         */
        private static ValidationService.DateValidator datVal;
    
        static {
            // Instantiate instances of each validator for later use
            // A new validator could be created each time, but this makes code neater
            strVal = new ValidationService.StringValidator();
            numVal = new ValidationService.NumericValidator();
            datVal = new ValidationService.DateValidator();
        }
    
        /**
         * Handles fetching data for a basic order. This means a list of product IDs, a list of quantities of each of
         * these products, a staff ID and an order date.
         * This basic information is required for the first three options, so it makes sense to package it up.
         * Furthermore, it's slightly more intuitive to alternate between requesting a product ID, and the quantity for
         * this same product, versus requesting a list of all IDs, then a list of all quantities for each corresponding
         * ID.
         */
        private static class BaseProductData {
            /**
             * An array of product IDs.
             */
            private int[] productIds;
            /**
             * An array of quantities for these product IDs.
             */
            private int[] quantities;
            /**
             * The ID of the staff member making the order.
             */
            private int staffId;
            /**
             * The date that the order was made.
             */
            private String orderDate;
    
            /**
             * Repeatedly requests a new product ID and a corresponding quantity for this product, until the list is
             * terminated with a product ID of 0.
             * @throws ClientValidationError If any input at any point fails to validate.
             */
            private void inputProductList() throws ClientValidationError {
                ArrayList<Integer> temporaryProductIds = new ArrayList<>();
                ArrayList<Integer> temporaryQuantities = new ArrayList<>();
                boolean loop = true;

                // Get all pairs of product ID and quantities
                while (loop) {
                    int newId;
                    int newQuantity;

                    newId = interrogateNumeric("Please enter product ID (0 to terminate)", numVal);

                    // If we need to terminate, do so
                    if (newId == 0) {
                        loop = false;
                    // Otherwise, get a quantity
                    } else {
                        newQuantity = interrogateNumeric("Please enter product quantity", numVal);

                        // Add ID and quantity to each array list
                        temporaryProductIds.add(newId);
                        temporaryQuantities.add(newQuantity);
                    }
                }

                // Write the array lists to corresponding arrays, because we need to give inputs like that.
                productIds = new int[temporaryProductIds.size()];
                quantities = new int[temporaryQuantities.size()];
                for (int i = 0; i < productIds.length; i++) {
                    productIds[i] = temporaryProductIds.get(i);
                    quantities[i] = temporaryQuantities.get(i);
                }
            }
    
            /**
             * Requests a date from the user for the order.
             * @throws ClientValidationError If the date entered is invalid in some way
             */
            private void inputOrderDate() throws ClientValidationError {
                orderDate = interrogate("Please enter the date of the order", datVal);
            }
    
            /**
             * Requests a staff ID from the user
             * @throws ClientValidationError If the ID entered is invalid in some way
             */
            private void inputStaffId() throws ClientValidationError {
                staffId = interrogateNumeric("Please enter your staff ID", numVal);
            }
    
            /**
             * Gets the array of product IDs.
             * @return The array of product IDs.
             */
            public int[] getProductIds() {
                return (productIds);
            }
    
            /**
             * Gets the array of quantities.
             * @return The array of quantities.
             */
            public int[] getQuantities() {
                return (quantities);
            }
    
            /**
             * Gets the date the order was made.
             * @return The date the order was made.
             */
            public String getOrderDate() {
                return (orderDate);
            }
    
            /**
             * Gets the staff ID of whoever entered the order
             * @return The staff ID of whoever entered the order
             */
            public int getStaffId() {
                return (staffId);
            }
    
            /**
             * Constructor method that fills in information.
             * @throws ClientValidationError If any of the inputs fail at any point.
             */
            public BaseProductData () throws ClientValidationError {
                inputProductList();
                inputOrderDate();
                inputStaffId();
            }
        }
    
        /**
         * Handles fetching information about a customer. This is relevant for collection or deliveries. Since both
         * require slightly different information, but with some slight overlap, the class only collects data relevant
         * to both at first.
         * Either the {@see #inputDelivery} or {@see #inputCollection} methods must be used to "prime" the class to
         * provide data for either delivery or collections. Since the type of data we own can't be known until we're
         * instantiated, a couple of "guards" are provided. If collection/delivery information is provided twice, an
         * exception is thrown. If an attempt is made to retrieve either before the class has collected this data, the
         * same exception is thrown.
         */
        private static class CustomerInformation {
            /**
             * The first name of the customer
             */
            private String fName;
            /**
             * The second name of the customer
             */
            private String lName;
    
            /**
             * The collection date for the order
             */
            private String collectionDate;
            /**
             * The guard for turning this {@code CustomerInformation} object into one that deals with collections.
             */
            private boolean collectionSet;
    
            /**
             * The delivery date for the order
             */
            private String deliveryDate;
            /**
             * The house number/name for the order
             */
            private String house;
            /**
             * The street address for the order
             */
            private String street;
            /**
             * The city/town/etc. for the order
             */
            private String city;
            /**
             * The guard for turning this {@code CustomerInformation} object into one that deals with deliveries.
             */
            private boolean deliverySet;
    
            /**
             * Collects basic name information common to both order types.
             * @throws ClientValidationError If either name provided is the empty string.
             */
            private void inputName() throws ClientValidationError {
                fName = interrogate("What is the customer's first name?", strVal);
                lName = interrogate("What is the customer's second name?", strVal);
            }
    
            /**
             * Requests information for a collection order (i.e., the collection date). Disables the guard blocking
             * retrieval of collection data.
             * @throws ClientValidationError If the date provided is not in the correct format
             * @throws IllegalStateException If this method is called twice for one instance of this class
             */
            public void inputCollection() throws ClientValidationError, IllegalStateException {
                if (!collectionSet) {
                    collectionDate = interrogate("What is the collection date?", datVal);

                    collectionSet = true;
                } else {
                    throw new IllegalStateException("Collection information retrieved twice");
                }
            }
    
            /**
             * Requests information for a delivery order (i.e., a delivery date, and an address in three parts).
             * Disables the guard blocking retrieval of delivery data.
             * @throws ClientValidationError If any information provided fails to validate.
             * @throws IllegalStateException If this method is called twice for one instance of this class.
             */
            public void inputDelivery() throws ClientValidationError, IllegalStateException {
                if (!deliverySet) {
                    deliveryDate = interrogate("What is the delivery date?", datVal);
                    house = interrogate("What is the house number?", strVal);
                    street = interrogate("What is the street?", strVal);
                    city = interrogate("What is the city?", strVal);

                    deliverySet = true;
                } else {
                    throw new IllegalStateException("Delivery information retrieved twice");
                }
            }
    
            /**
             * An override which assumes that the guard allows the retrieval. This function actually doesn't do
             * anything, and {@code value} is equivalent to {@code retrieveData(value)}, but this makes formatting of
             * code neater below.
             * @param value The value to return
             * @return The data provided (always)
             */
            private String retrieveData(String value) {
                return (retrieveData(value, true));
            }
    
            /**
             * Attempts to retrieve the provided data, but only if the provided guard allows it.
             * @param value The data we want to retrieve.
             * @param guard The guard we need to ask permission from to retrieve the data
             * @return The data we want to retrieve ({@code value})
             * @throws IllegalStateException If the guard blocks retrieval of the data
             */
            private String retrieveData(String value, boolean guard) throws IllegalStateException {
                if (guard) {
                    return value;
                } else {
                    throw new IllegalStateException("Data requested before set");
                }
            }
    
            /**
             * Retrieves the customer's first name.
             * @return The customer's first name.
             */
            public String getFName()            { return (retrieveData(fName)); }
    
            /**
             * Retrieves the customer's last name.
             * @return The customer's last name.
             */
            public String getLName()            { return (retrieveData(lName)); }
    
            /**
             * Retrieves the order's collection date, if this order is a collection.
             * @return The order's collection date.
             * @throws IllegalStateException If {@see #inputCollection} has not been called first.
             */
            public String getCollectionDate()   { return (retrieveData(collectionDate, collectionSet)); }
    
            /**
             * Retrieves the order's delivery date, if this order is a delivery.
             * @return The order's delivery date.
             * @throws IllegalStateException If {@see #inputDelivery} has not been called first.
             */
            public String getDeliveryDate()     { return (retrieveData(deliveryDate, deliverySet)); }
            
            /**
             * Retrieves the order's house address, if this order is a delivery.
             * @return The order's house address.
             * @throws IllegalStateException If {@see #inputDelivery} has not been called first.
             */
            public String getHouse()            { return (retrieveData(house,        deliverySet)); }
            
            /**
             * Retrieves the order's street address, if this order is a delivery.
             * @return The order's street address.
             * @throws IllegalStateException If {@see #inputDelivery} has not been called first.
             */
            public String getStreet()           { return (retrieveData(street,       deliverySet)); }
            
            /**
             * Retrieves the order's city address, if this order is a delivery.
             * @return The order's city address.
             * @throws IllegalStateException If {@see #inputDelivery} has not been called first.
             */
            public String getCity()             { return (retrieveData(city,         deliverySet)); }
    
            /**
             * Retrieves basic information common to all orders (customer name), and enables the guards against
             * retrieving either collection or delivery data.
             * @throws ClientValidationError If validation of any input fails.
             */
            public CustomerInformation () throws ClientValidationError {
                inputName();

                collectionSet = false;
                deliverySet = false;
            }
        }
    
        /**
         * Gets user input required for menu option 1, and then performs menu option 1.
         * (In store purchase).
         * @param conn The connection to database.
         */
        public static void inputOption1(Connection conn) {
            BaseProductData container;
            try {
                container = new InputHandler.OptionHandler.BaseProductData();
            } catch (ClientValidationError e) {
                System.out.println("The information you entered is invalid.");
                return;
            }
    
            int[]   productIDs = container.getProductIds();
            int[]   quantities = container.getQuantities();
            String  orderDate  = container.getOrderDate();
            int     staffID    = container.getStaffId();
    
            Assignment.option1(conn, productIDs, quantities, orderDate, staffID);
        }
    
        /**
         * Gets user input required for menu option 2, and then performs menu option 2.
         * (Collection)
         * @param conn The connection to the database.
         */
        public static void inputOption2(Connection conn) {
            BaseProductData container;
            CustomerInformation customer;
            
            try {
                container = new InputHandler.OptionHandler.BaseProductData();
                customer = new InputHandler.OptionHandler.CustomerInformation();
                customer.inputCollection();
            } catch (ClientValidationError e) {
                System.out.println("The information you entered is invalid.");
                return;
            }
    
            int[] productIds        = container.getProductIds();
            int[] quantities        = container.getQuantities();
            String orderDate        = container.getOrderDate();
            int staffId             = container.getStaffId();
            String collectionDate   = customer.getCollectionDate();
            String fName            = customer.getFName();
            String lName            = customer.getLName();
    
            Assignment.option2(conn, productIds, quantities, orderDate, collectionDate, fName, lName, staffId);
        }
    
        /**
         * Gets user input required for menu option 3, and then performs menu option 3.
         * (Delivery)
         * @param conn The connection to the database.
         */
        public static void inputOption3(Connection conn)  {
            BaseProductData container;
            CustomerInformation customer;
            
            try {
                container = new InputHandler.OptionHandler.BaseProductData();
                customer = new InputHandler.OptionHandler.CustomerInformation();
                customer.inputDelivery();
            } catch (ClientValidationError e) {
                System.out.println("The information you entered is invalid.");
                return;
            }

            int[] productIds        = container.getProductIds();
            int[] quantities        = container.getQuantities();
            String orderDate        = container.getOrderDate();
            int staffId             = container.getStaffId();
            
            String deliveryDate = customer.getDeliveryDate();
            String fName = customer.getFName();
            String lName = customer.getLName();
            String house = customer.getHouse();
            String street = customer.getStreet();
            String city = customer.getCity();

             Assignment.option3(conn, productIds, quantities, orderDate, deliveryDate, fName, lName, house, street,
                     city, staffId);
        }

        /**
         * Gets user input required for menu option 5, and then performs menu option 5
         * (Cleanup uncollected collections)
         * @param conn The connection to the database
         */
        public static void inputOption5(Connection conn) {
            String date;

            try {
                date = interrogate("Enter the date", datVal);
            } catch (ClientValidationError e) {
                System.out.println("Date provided is invalid");
                return;
            }

            Assignment.option5(conn, date);
        }

        /**
         * Gets user input required for menu option 8, and then performs menu option 8
         * (Employee rewards)
         * @param conn The connection to the database
         */
        public static void inputOption8(Connection conn) {
            int year;
            try {
                year = interrogateNumeric("Enter the year (YYYY)", numVal);
            } catch (ClientValidationError e) {
                System.out.println("Year provided is invalid");
                return;
            }

            if (year < 1950 || year >= 2050) {
                // Sqlplus considers all dates from 01-Jan-51 to 31-Dec-99 to be in the 1900s
                System.out.println("Year must between 1950 and 2050");
            } else {
                Assignment.option8(conn, year);
            }
        }
    }
}

/**
 * Class to handle various formatting tasks throughout the program
 */
class Formatting {
    /**
     * Formats currency as GBP
     * @param currency What we want formatted as currency
     * @return A string in the format of currency, e.g., £7.50
     */
    public static String formatCurrency(double currency) {
        Locale locale = new Locale("en", "GB");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        return (formatter.format(currency));
    }

    /**
     * Handles converting data into CSV outputs.
     * Works by passing ArrayLists of String objects to serve as the rows and cells of the table, respectively.
     * Headers are counted separately from rows.
     * Column size is enforced based on the dimensions of the header row
     */
    public static class TabularData {
        /**
         * Header row of the table
         */
        private ArrayList<String> headers;
        /**
         * Various rows of the table - must be of the same length as the headers
         */
        private ArrayList<ArrayList<String>> data;
        /**
         * Explicitly maintain column count - used to enforce cell count in new rows
         */        
        private int columns;

        /**
         * A table must be created with headers.
         * Set up the number of columns, and create a new, empty array for rows
         * @param headers The headers of the table
         */
        public TabularData (ArrayList<String> headers) {
            data = new ArrayList<>();
            this.headers = headers;
            columns = headers.size();
        }

        /**
         * Add a new row to the table
         * @param row The new row to add to the table
         * @throws IllegalArgumentException If the number of cells in the new row is incorect (based on the number of
         *         cells in the header row.)
         */
        public void add(ArrayList<String> row) {
            if (row.size() == columns) {
                // Add data to table
                data.add(row);
            } else {
                // Enforce dimensions
                throw new IllegalArgumentException("Invalid row count");
            }
        }

        /**
         * Adds a series of rows to the table, provided by an Iterable of ArrayLists.
         * @param rows A series of rows to add to the table
         * @throws IllegalArgumentException If any rows do not satisfy column count requirements. All rows added prior
         *                                  to exception throwing will remain in the table.
         */
        public void addRows(Iterable<ArrayList<String>> rows) {
            for (ArrayList<String> row : rows) {
                add(row);
            }
        }

        /**
         * Gets the number of rows in the table, not including the headers
         * @return The number of rows in the table, not including the headers
         */
        public int getRowCount() {
            return (data.size());
        }

        /**
         * Converts the table's data and headers to a CSV
         * @return The table's headers and data converted to a CSV
         */
        private String getTable() {
            StringBuilder tableOut = new StringBuilder();

            tableOut.append(getRow(headers));
            tableOut.append("\n");

            Iterator<ArrayList<String>> i = data.iterator();
            while (i.hasNext()) {
                String currentRow = getRow(i.next());
                tableOut.append(currentRow);
                if (i.hasNext()) {
                    tableOut.append("\n");
                }
            }

            return (tableOut.toString());
        }

        /**
         * Converts a given row of the table to a line of CSV
         * @return The given row, converted to a line of CSV
         */
        private String getRow(ArrayList<String> row) {
            StringBuilder rowOut = new StringBuilder();
            Iterator<String> i = row.iterator();
            while (i.hasNext()) {
                rowOut.append(i.next());
                if (i.hasNext()) {
                    rowOut.append(", ");
                }
            }
            return (rowOut.toString());
        }

        /**
         * Coverts the entire table to a CSV string, headers and all
         * @return The entire table converted to a CSV string
         */
        public String toString() {
            return (getTable());
        }
    }
}

class Assignment {
    /**
     * Checks to see if the provided query returns at least one reuslt.
     * @param conn Connection to the database
     * @param query Query to test
     * @throws SQLException If there is an error when attempting to fetch the query
     */
    private static boolean checkValid(Connection conn, String query) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        return (rs.next());
    }

    /**
     * Performs tasks common to options 1-3
     * @param productIDs An array of productIDs associated with an order
     * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in
     *        productIDs
     * @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
     * @param staffID The id of the staff member who sold the order
     * @return The ID of the new entry into orders on success. -1 otherwise.
     */
    private static int standardOrder(Connection conn, int[] productIDs, int[] quantities, String orderDate,
            int staffID, String orderType, int completed) {
        int newOrderPrimaryKey;
        int currentQuantities[];
        String orders__creationStatement;
        String staff_orders__creationStatement;
        String order_products__creationStatement;
        String inventory__checkStatement;
        String inventory__updateStatement;
        
        // Check inputs are valid
        try {
            if (!checkValid(conn, "SELECT * FROM staff WHERE StaffID = " + staffID)) {
                System.out.println("Staff ID was invalid");
                return (-1);
            }
            
            if (!checkValid(conn, "SELECT TO_DATE('" + orderDate + "') FROM dual")) {
                System.out.println("Order date was invalid");
                return (-1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred when testing inputs.");
        }
        
        // Get currently remaining stock
        currentQuantities = new int[productIDs.length];
        for (int i = 0; i < productIDs.length; i++) {
            int productID = productIDs[i];
            int quantity = quantities[i];
            inventory__checkStatement = "SELECT ProductStockAmount FROM inventory WHERE ProductID = " + productID;
            
            // Load currently remaining stock for each item from the database
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(inventory__checkStatement);
                if (!rs.next()) {
                    System.out.println("Invalid product ID: " + productID);
                    return (-1);
                }
                currentQuantities[i] = rs.getInt("PRODUCTSTOCKAMOUNT");
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error occurred checking stock quantity.");
                return (-1);
            }
            
            // Ensure the remaining stock is sufficient for the order to complete
            if (currentQuantities[i] < quantity) {
                System.out.println("Insufficient quantity for product " + productID + ": " + quantity + " requested " +
                        "but only " + currentQuantities[i] + " remains.");
                return (-1);
            } else {
                // Update for when we return stock values at the end
                currentQuantities[i] = currentQuantities[i] - quantity;
            }
        }
        
        // Get primary key of new order
        try {
            Statement stmt = conn.createStatement();
            ResultSet pkQuery = stmt.executeQuery("SELECT pk_order.nextval FROM dual");
            pkQuery.next();
            newOrderPrimaryKey = pkQuery.getInt("NEXTVAL");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error finding primary key");
            return (-1);
        }
    
        // Construct orders and staff_orders queries
        orders__creationStatement = "INSERT INTO orders (OrderID, OrderType, OrderCompleted, OrderPlaced) VALUES (" +
                newOrderPrimaryKey + ", '" + orderType + "', " + completed + ", '" + orderDate + "')";
        staff_orders__creationStatement = "INSERT INTO staff_orders (OrderID, StaffID) VALUES (" + newOrderPrimaryKey +
                ", " + staffID + ")";
        
        // Run orders query
        try {
            Statement stmt = conn.createStatement();
            stmt.executeQuery(orders__creationStatement);
        } catch (SQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
            System.out.println("Order date invalid");
            return (-1);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred.");
            return (-1);
        }
    
        // Run staff_orders query
        try {
            Statement stmt = conn.createStatement();
            stmt.executeQuery(staff_orders__creationStatement);
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Staff ID invalid");
            return (-1);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred.");
            return (-1);
        }
        
        // Create order_products entries, and update quantities in inventory
        for (int i = 0; i < productIDs.length; i++) {
            int productId = productIDs[i];
            int quantity = quantities[i];
            
            order_products__creationStatement = "INSERT INTO order_products (OrderID, ProductID, ProductQuantity)" +
                    "VALUES (" + newOrderPrimaryKey + ", " + productId + ", " + quantity + ")";
            inventory__updateStatement = "UPDATE inventory SET ProductStockAmount = ProductStockAmount - " + quantity +
                    " WHERE ProductID = " + productId;
            
            // Create the order
            try {
                conn.createStatement().executeQuery(order_products__creationStatement);
            } catch (SQLIntegrityConstraintViolationException e) {
                System.out.println("Product ID (" + productId + ") or quantity (" + quantity + ") invalid");
                return (-1);
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error occurred");
                return (-1);
            }
            
            // Update inventory stock levels
            try {
                Statement stmt = conn.createStatement();
                stmt.executeQuery(inventory__updateStatement);
            } catch (SQLIntegrityConstraintViolationException e) {
                // Shouldn't ever happen, because we checked at the start
                System.out.println("Insufficient product remains. Database integrity compromised.");
                return (-1);
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error occurred");
                return (-1);
            }
        }
        
        // Output new product quantities
        for (int i = 0; i < productIDs.length; i++) {
            StringBuilder out = new StringBuilder("Product ID ");
            out.append(productIDs[i]);
            out.append(" ");
            if (i == 0) {
                out.append("stock ");
            }
            out.append("is now at ");
            out.append(currentQuantities[i]);
            
            System.out.println(out.toString());
        }

        return (newOrderPrimaryKey);
    }
    
    /**
     * @param conn An open database connection
     * @param productIDs An array of productIDs associated with an order
     * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in
     *        productIDs
     * @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
     * @param staffID The id of the staff member who sold the order
     */
    public static void option1(Connection conn, int[] productIDs, int[] quantities, String orderDate, int staffID)
    {
        standardOrder(conn, productIDs, quantities, orderDate, staffID, "InStore", 1);
    }

    /**
     * @param conn An open database connection
     * @param productIDs An array of productIDs associated with an order
     * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in
     *        productIDs
     * @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
     * @param collectionDate A string in the form of 'DD-Mon-YY' that represents the date the order will be collected
     * @param fName The first name of the customer who will collect the order
     * @param LName The last name of the customer who will collect the order
     * @param staffID The id of the staff member who sold the order
     */
    public static void option2(Connection conn, int[] productIDs, int[] quantities, String orderDate,
            String collectionDate, String fName, String lName, int staffID)
    {
        String collections__creationStatement;
        int orderPrimaryKey;

        // Fulfill standard order tasks
        orderPrimaryKey = standardOrder(conn, productIDs, quantities, orderDate, staffID, "Collection", 0);

        // If success, insert entry into collections table
        if (orderPrimaryKey != -1) {
            collections__creationStatement = "INSERT INTO collections (OrderID, FName, LName, CollectionDate) VALUES" +
                    " (" + orderPrimaryKey + ", '" + fName + "', '" + lName + "', '" + collectionDate + "')";

            // Make the insertion
            try {
                Statement stmt = conn.createStatement();
                stmt.executeQuery(collections__creationStatement);
            } catch (SQLIntegrityConstraintViolationException e) {
                System.out.println("Collection date invalid");                
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error occurred.");
            }
        }
    }

    /**
     * @param conn An open database connection
     * @param productIDs An array of productIDs associated with an order
     * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in
     *        productIDs
     * @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
     * @param deliveryDate A string in the form of 'DD-Mon-YY' that represents the date the order will be delivered
     * @param fName The first name of the customer who will receive the order
     * @param LName The last name of the customer who will receive the order
     * @param house The house name or number of the delivery address
     * @param street The street name of the delivery address
     * @param city The city name of the delivery address
     * @param staffID The id of the staff member who sold the order
     */
    public static void option3(Connection conn, int[] productIDs, int[] quantities, String orderDate,
            String deliveryDate, String fName, String lName, String house, String street, String city, int staffID)
    {
        String deliveries__creation_statement;
        int orderPrimaryKey;

        // Fulfill standard order tasks
        orderPrimaryKey = standardOrder(conn, productIDs, quantities, orderDate, staffID, "Delivery", 0);

        // If success, insert entry into deliveries table
        if (orderPrimaryKey != -1) {
            deliveries__creation_statement = "INSERT INTO deliveries (OrderID, FName, LName, DeliveryDate, House, " +
                    "Street, City) VALUES (" + orderPrimaryKey + ", '" + fName + "', '" + lName + "', '" + deliveryDate + 
                    "', '" + house + "', '" + street + "', '" + city + "')";
            
            // Make the insertion
            try {
                Statement stmt = conn.createStatement();
                stmt.executeQuery(deliveries__creation_statement);
            } catch (SQLIntegrityConstraintViolationException e) {
                System.out.println("Delivery date was invalid");
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error occurred.");
            }
        }
    }

    /**
     * @param conn An open database connection
     */
    public static void option4(Connection conn)
    {
        String biggestSellers = "SELECT * FROM FormattedValueSold";

        ArrayList<String> headers = new ArrayList<String>();
        headers.add("ProductID");
        headers.add("ProductDesc");
        headers.add("TotalValueSold");

        Formatting.TabularData table = new Formatting.TabularData(headers);

        // Load data from database
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(biggestSellers);

            while (rs.next()) {
                // Construct new row from current row of returned data
                ArrayList<String> newRow = new ArrayList<>();
                String id = rs.getString("ProductID");
                String desc = rs.getString("ProductDesc");
                String total = Formatting.formatCurrency(rs.getDouble("Value"));

                newRow.add(id);
                newRow.add(desc);
                newRow.add(total);

                // Add newly constructed row to table
                table.add(newRow);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in application");
        }

        // Output the CSV
        System.out.println(table.toString());
    }

    /**
     * @param conn An open database connection
     * @param date The target date to test collection deliveries against
     */
    public static void option5(Connection conn, String date)
    {
        String outputQuery = ""
                + "SELECT op.ProductID, op.OrderID, op.ProductQuantity\n"
                + "FROM collections c\n"
                + "JOIN orders o ON o.OrderID = c.OrderID\n"
                + "JOIN order_products op ON op.OrderID = c.OrderID\n"
                + "WHERE c.CollectionDate > TO_DATE('" + date + "') + 8 AND o.OrderCompleted = 0";

        String cleanupScript = ""
                + "BEGIN\n"
                + "FOR record IN (" + outputQuery + ")\n"
                + "LOOP\n"
                    + "DELETE FROM collections c WHERE c.OrderID = record.OrderID;\n"
                    + "DELETE FROM staff_orders so WHERE so.OrderID = record.OrderID;\n"
                    + "DELETE FROM order_products op WHERE op.OrderID = record.OrderID;\n"
                    + "DELETE FROM orders o WHERE o.OrderID = record.OrderID;\n"
                    + "UPDATE inventory\n"
                    + "SET ProductStockAmount = ProductStockAmount + record.ProductQuantity\n"
                    + "WHERE ProductID = record.ProductID;\n"
                + "END LOOP;\n"
                + "END;\n";

        try {
            checkValid(conn, "SELECT TO_DATE('" + date + "') FROM dual");
        } catch (SQLException e) {
            System.out.println("Date is invalid");
            return;
        }

        StringBuilder out = new StringBuilder();

        try {
            Statement results = conn.createStatement();
            Statement cleanup = conn.createStatement();

            ResultSet rs =  results.executeQuery(outputQuery);
            cleanup.execute(cleanupScript);

            while (rs.next()) {
                int productID = rs.getInt("ProductID");
                out.append("Order " + productID + " has been cancelled\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred when cleaning database");
            return;
        }

        System.out.println(out.toString());
    }

    /**
     * @param conn An open database connection
     */
    public static void option6(Connection conn)
    {
        String topSellersQuery = "SELECT * FROM FormattedTopSellers";

        ArrayList<String> headers = new ArrayList<>();
        headers.add("EmployeeName");
        headers.add("TotalValueSold");

        Formatting.TabularData table = new Formatting.TabularData(headers);

        // Load data from database
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(topSellersQuery);

            while (rs.next()) {
                // Construct new row of table from current row of returned data
                ArrayList<String> newRow = new ArrayList<>();
                String name = rs.getString("EmployeeName");
                String value = Formatting.formatCurrency(rs.getDouble("TotalValueSold"));

                newRow.add(name);
                newRow.add(value);

                // Add newly constructed row to table
                table.add(newRow);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in application");
            return;
        }

        // Output the CSV
        System.out.println(table.toString());
    }

    /**
     * @param conn An open database connection
     */
    public static void option7(Connection conn)
    {
        // Gets a list of all sales of all top selling products by all staff
        String topSellerSalesQuery = "SELECT * FROM TopSellerSalesByStaff";
        // Effectively a 2D hash map - Indexed by employee name, then by the product ID
        // The value stored is the quantity of the given item they sold
        HashMap<String, HashMap<Integer, Integer>> salesRecord = new HashMap<>();
        // The hash set is used to ensure values are printed for all items for all staff, even if they didn't sell any
        HashSet<Integer> allProductIDs = new HashSet<>();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(topSellerSalesQuery);

            while (rs.next()) {
                HashMap<Integer, Integer> staffSales;

                String employee = rs.getString("EmployeeName");
                int productID = rs.getInt("ProductID");
                int quantity = rs.getInt("Quant");

                // Make sure product is in hash set
                allProductIDs.add(productID);

                // Make sure staff member is in hashmap
                if (!salesRecord.containsKey(employee)) {
                    staffSales = new HashMap<>();
                    salesRecord.put(employee, staffSales);
                } else {
                    staffSales = salesRecord.get(employee);
                }

                // Add to old quantity if already present
                if (staffSales.containsKey(productID)) {
                    quantity = quantity + staffSales.get(productID);
                }
                staffSales.put(productID, quantity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in application");
            return;
        }

        // Construct table and headers
        ArrayList<String> headers = new ArrayList<>();
        headers.add("EmployeeName");
        for (int id : allProductIDs) {
            headers.add("Product " + id);
        }
        Formatting.TabularData table = new Formatting.TabularData(headers);

        // Construct rows of CSV table
        for (String employeeName : salesRecord.keySet()) {
            ArrayList<String> newRow = new ArrayList<>();
            // Get the entry in the sales records that corresponds to this employee
            HashMap<Integer, Integer> employeeRow = salesRecord.get(employeeName);
            newRow.add(employeeName);
            
            for (int id : allProductIDs) {
                String quantity;
                // For every top selling item (given by the set), find the number of items this employee sold
                if (employeeRow.containsKey(id)) {
                    quantity = employeeRow.get(id).toString();
                } else {
                    // Absence is taken as 0 sold
                    quantity = "0";
                }

                // Add to row
                newRow.add(quantity);
            }

            // Add row to table
            table.add(newRow);
        }

        // Output table
        System.out.println(table.toString());
    }

    /**
     * @param conn An open database connection
     * @param year The target year we match employee and product sales against
     */
    public static void option8(Connection conn, int year)
    {
        String dateBoundary = "01-Jan-" + (year + "").substring(2);

        String rewardsQuery = ""
                // Select all staff members who've sold more than 30000 worth of anything in the given year
                + "SELECT (staff.FName || ' ' || staff.LName) EmployeeName\n"
                + "FROM (SELECT so.StaffID, SUM(i.ProductPrice * op.ProductQuantity) Value\n"
                      + "FROM staff_orders so\n"
                      + "JOIN orders o ON o.OrderID = so.OrderID\n"
                      + "JOIN order_products op ON op.OrderID = so.OrderID\n"
                      + "JOIN inventory i ON i.ProductID = op.ProductID\n"
                      + "GROUP BY so.StaffID\n"
                      + ") sellers\n"
                + "JOIN staff ON staff.StaffID = sellers.StaffID\n"
                + "WHERE Value > 30000\n"
                  // Make sure nothing is left when subtracting the set of all products the staff member has sold from
                  // the set of all products which have sold more than 20000 in the given year
                  + "AND (NOT EXISTS (\n"
                          + "(\n"
                              // Get a list of all the product IDs which have sold > 20000 in the given year
                              + "SELECT ProductID FROM (\n"
                                  + "SELECT i.ProductID, SUM(i.ProductPrice * op.ProductQuantity) val FROM inventory i\n"
                                  + "JOIN order_products op ON op.ProductID = i.ProductID\n"
                                  + "JOIN orders o ON o.OrderID = op.OrderID\n"
                                  + "WHERE o.OrderPlaced > TO_DATE('01-Jan-17')\n"
                                    + "AND o.OrderPlaced < TO_DATE('01-Jan-17') + 365\n"
                                  + "GROUP BY i.ProductID\n"
                              + ")\n"
                              + "WHERE val > 20000\n"
                          + ")\n"
                          + "MINUS\n"
                          // Get a list of all products the staff member has sold in the given year
                          + "(\n"
                              + "SELECT DISTINCT op.ProductID\n"
                              + "FROM staff_orders so\n"
                              + "JOIN order_products op ON op.OrderID = so.OrderID\n"
                              + "JOIN orders o ON o.OrderID = op.OrderID\n"
                              + "WHERE o.OrderPlaced > TO_DATE('01-Jan-17')\n"
                                + "AND o.OrderPlaced < TO_DATE('01-Jan-17') + 365\n"
                                + "AND so.StaffID = sellers.StaffID\n"
                          + ")\n"
                      + "))\n";

        // Ensure provided date is valid
        try {
            if (!checkValid(conn, "SELECT TO_DATE('" + dateBoundary + "') FROM dual")) {
                System.out.println("Invalid date");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error executing date check");
            return;
        }

        // Run query and load results
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(rewardsQuery);

            ArrayList<String> headers = new ArrayList<>();
            headers.add("EmployeeName");
            Formatting.TabularData table = new Formatting.TabularData(headers);

            // Load results into table
            while (rs.next()) {
                ArrayList<String> row = new ArrayList<>();
                String name = rs.getString("EmployeeName");
                row.add(name);
                table.add(row);
            }

            // Output table results
            System.out.println(table.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error when getting reward-ees");
            return;
        }
    }
    
    /**
     * Gets the user's credentials, and uses them to connect to the database.
     * @return The connection object provided by connecting to the database.
     */
    public static Connection getConnection()
    {
        String[] credentials;
        Connection conn;

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException x) {
            System.out.println("Driver could not be loaded");
        }

        conn = null;
        try {
            credentials = InputHandler.getCredentials();

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
    
    /**
     * Cleans up just before the program exits.
     * @param conn The database connection to close.
     * @throws SQLException If there is an error when closing the database connection.
     */
    public static void exit(Connection conn) throws SQLException {
        conn.close();

        System.out.println("Database system exited.");
    }
    
    /**
     * Provides a menu to the user so that they may make selections.
     * @return A menu formatted as a single string.
     */
    private static String getMenu() {
        StringBuilder menu = new StringBuilder();
        
        for (int i = 0; i < 3; i++) menu.append("\n");
    
        menu.append("1) In-Store purchase\n");
        menu.append("2) Collection\n");
        menu.append("3) Delivery\n");
        menu.append("4) Biggest sellers\n");
        menu.append("5) Reserved stock\n");
        menu.append("6) Staff life-time success\n");
        menu.append("7) Staff contribution\n");
        menu.append("8) Employees of the year\n");
        menu.append("0) Quit\n");
        
        return (menu.toString());
    }

    public static void main(String args[]) throws SQLException, IOException
    {
        // You should only need to fetch the connection details once
        Connection conn = getConnection();
        boolean loop = true;
        int input;

        while (loop) {
            // Attempt to get a selection
            try {
                System.out.println(getMenu());
                input = InputHandler.menuOption("Please make a selection >> ");
            } catch (InvalidMenuSelection e) {
                System.out.println("Please enter a valid selection.\n\n");
                input = -1;
            }

            // Attempt to work out what the user wanted based on their selection.
            switch (input) {
                case 1:     InputHandler.OptionHandler.inputOption1(conn);
                            break;
                case 2:     InputHandler.OptionHandler.inputOption2(conn);
                            break;
                case 3:     InputHandler.OptionHandler.inputOption3(conn);
                            break;
                case 4:     option4(conn);
                            break;
                case 5:     InputHandler.OptionHandler.inputOption5(conn);
                            break;
                case 6:     option6(conn);
                            break;
                case 7:     option7(conn);
                            break;
                case 8:     InputHandler.OptionHandler.inputOption8(conn);
                            break;
                case 0:     loop = false;
                            System.out.println("Goodbye.");
                            break;
                default:    System.out.println("Invalid selection.");
            }
        }
        
        exit(conn);
    }
}
