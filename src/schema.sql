-- Sequences are used to assign a primary key to each of the three main tables (staff, orders, inventory)
-- The next value of the primary key is queried from the sequence when one needs to be created, and then can be used
-- through the rest of the method when creating new entries that reference this one.
CREATE SEQUENCE pk_staff
START WITH 1001
INCREMENT BY 1;

CREATE SEQUENCE pk_order
START WITH 1001
INCREMENT BY 1;

CREATE SEQUENCE pk_inventory
START WITH 1001
INCREMENT BY 1;

CREATE TABLE inventory (
    ProductID INTEGER PRIMARY KEY,
    ProductDesc VARCHAR(30) NOT NULL,
    ProductPrice NUMERIC(8, 2) NOT NULL,
    ProductStockAmount INTEGER NOT NULL,
    -- IDs should never be negative or zero
    CONSTRAINT PositiveProductID CHECK (ProductID > 0),
    -- Stock should never fall below 0
    CONSTRAINT PostiiveStock CHECK (ProductStockAmount >= 0),
    -- A price should never be negative or zero for an item
    CONSTRAINT PositivePrice CHECK (ProductPrice > 0)
);

CREATE TABLE orders (
    OrderID INTEGER PRIMARY KEY,
    OrderType VARCHAR(30) NOT NULL,
    OrderCompleted INTEGER NOT NULL,
    OrderPlaced DATE NOT NULL,
    -- IDs should never be negative or zero
    CONSTRAINT PositiveOrderID CHECK (OrderID > 0),
    -- The OrderCompleted field must always be either 0 or 1
    CONSTRAINT OrderCompletedBoolean CHECK (OrderCompleted >= 0 AND OrderCompleted <= 1),
    -- The OrderType field must always be either "InStore", "Delivery" or "Collection", depending on type
    CONSTRAINT OrderTypeEnum CHECK (OrderType = 'InStore' OR OrderType = 'Delivery' OR OrderType = 'Collection')
);

CREATE TABLE order_products (
    OrderID INTEGER NOT NULL,
    ProductID INTEGER NOT NULL,
    ProductQuantity INTEGER NOT NULL,
    -- An order_products record referencing an orders record which does not exist should be impossible
    FOREIGN KEY (OrderID) REFERENCES orders(OrderID),
    -- An order_products record referencing an inventory record which does not exist should be impossible
    FOREIGN KEY (ProductID) REFERENCES inventory(ProductID),
    -- Product quantity should never be negative or zero
    CONSTRAINT PositiveQuantity CHECK (ProductQuantity > 0)
);

CREATE TABLE deliveries (
    OrderID INTEGER NOT NULL,
    FName VARCHAR(30) NOT NULL,
    LName VARCHAR(30) NOT NULL,
    House VARCHAR(30) NOT NULL,
    Street VARCHAR(30) NOT NULL,
    City VARCHAR(30) NOT NULL,
    DeliveryDate DATE NOT NULL,
    -- A delivery should never reference an order which does not exist
    FOREIGN KEY (OrderID) REFERENCES orders(OrderID),
    -- First name, last name, house, street and city should never be empty strings
    CONSTRAINT NonEmptyDeliveryFName CHECK (LENGTH(FName) > 0),
    CONSTRAINT NonEmptyDeliveryLName CHECK (LENGTH(LName) > 0),
    CONSTRAINT NonEmptyDeliveryHouse CHECK (LENGTH(House) > 0),
    CONSTRAINT NonEmptyDeliveryStreet CHECK (LENGTH(Street) > 0),
    CONSTRAINT NonEmptyDeliveryCity CHECK (LENGTH(City) > 0)
);

CREATE TABLE collections (
    OrderID INTEGER,
    FName VARCHAR(30) NOT NULL,
    LName VARCHAR(30) NOT NULL,
    CollectionDate DATE NOT NULL,
    -- A collection should never reference an order which does not exist
    FOREIGN KEY (OrderID) REFERENCES orders(OrderID),
    -- First and last names should never be empty strings
    CONSTRAINT NonEmptyCollectionFName CHECK (LENGTH(FName) > 0),
    CONSTRAINT NonEmptyCollectionLName CHECK (LENGTH(LName) > 0)
);

CREATE TABLE staff (
    StaffID INTEGER PRIMARY KEY,
    FName VARCHAR(30) NOT NULL,
    LName VARCHAR(30) NOT NULL,
    -- First and last names should never be empty strings
    CONSTRAINT NonEmptyStaffFName CHECK (LENGTH(FName) > 0),
    CONSTRAINT NonEmptyStaffLName CHECK (LENGTH(LName) > 0)
);

CREATE TABLE staff_orders (
    StaffID INTEGER,
    OrderID INTEGER,
    -- A staff_order record should never reference a staff member which doesn't exist
    FOREIGN KEY (StaffID) REFERENCES staff(StaffID),
    -- A staff_order record should never reference an order which doesn't exist
    FOREIGN KEY (OrderID) REFERENCES orders(OrderID)
);

--------------------
-- TRIGGERS BELOW --
--------------------

-- Below are triggers for auto-incrementing primary keys of the various fields
-- Since this is done Java-side since it makes a lot of the work a lot easier, these are removed for now

--CREATE OR REPLACE TRIGGER new_staff_member_id
--BEFORE INSERT ON staff
--    FOR EACH ROW BEGIN
--        :new.StaffID := pk_staff.nextval;
--    END;
--/
--
--CREATE OR REPLACE TRIGGER new_order_id
--BEFORE INSERT ON orders
--    FOR EACH ROW BEGIN
--        :new.OrderID := pk_order.nextval;
--    END;
--/
--
--CREATE OR REPLACE TRIGGER new_product_id
--BEFORE INSERT ON inventory
--    FOR EACH ROW BEGIN
--        :new.ProductID := pk_inventory.nextval;
--    END;
--/

-----------------
-- VIEWS BELOW --
-----------------

-- Get the quantity of sales for every product, including incomplete orders
CREATE VIEW ProductQuantitySold AS
SELECT i.ProductID id, NVL(SUM(op.ProductQuantity), 0) quantity FROM inventory i
LEFT JOIN order_products op ON op.ProductID = i.ProductID
GROUP BY i.ProductID
ORDER BY i.ProductID;

-- Get total value of sales from ProductQuantitySales
CREATE VIEW ProductValueSold AS
SELECT pqs.id, (pqs.quantity * i.ProductPrice) value FROM ProductQuantitySold pqs
JOIN inventory i ON i.ProductID = pqs.id;

-- Format the results of ProductValueSold (Option 4)
CREATE VIEW FormattedValueSold AS
SELECT i.ProductID ProductID,
       i.ProductDesc ProductDesc,
       NVL(pvs.value, 0) Value
FROM ProductValueSold pvs
JOIN inventory i ON i.ProductID = pvs.id
ORDER BY Value DESC;

-- Package together staff members by instances of products they've sold
CREATE VIEW StaffOrderData AS
SELECT so.StaffID, so.OrderID, op.ProductID, op.ProductQuantity FROM staff_orders so
JOIN orders o ON o.OrderID = so.OrderID
JOIN order_products op ON op.OrderID = so.OrderID;

-- Group sales of the same product together
CREATE VIEW StaffSaleProductQuantity AS
SELECT sod.StaffID, sod.ProductID, SUM(sod.ProductQuantity) quant FROM StaffOrderData sod
    -- Only used if incomplete orders don't count, but they do
    -- WHERE (SELECT OrderCompleted FROM orders WHERE OrderID = sod.OrderID) = 1
GROUP BY sod.StaffID, sod.ProductID
ORDER BY sod.StaffID;

-- Calculate the total value sold from the product price
CREATE VIEW StaffSaleProductValue AS
SELECT sspq.StaffID, sspq.ProductID, sspq.quant*i.ProductPrice value FROM StaffSaleProductQuantity sspq
JOIN inventory i ON i.ProductID = sspq.ProductID;

-- Calculate total value sold by staff member
CREATE VIEW StaffSaleTotalValue AS
SELECT sspv.StaffID, SUM(sspv.value) total FROM StaffSaleProductValue sspv
GROUP BY sspv.StaffID
ORDER BY total;

-- Format StaffSaleTotalValue so it's pretty
CREATE VIEW FormattedTopSellers AS
SELECT s.FName || ' ' || s.LName EmployeeName,
       NVL(sstv.total, 0) TotalValueSold
FROM StaffSaleTotalValue sstv
RIGHT JOIN staff S on s.StaffID = sstv.StaffID
WHERE sstv.total > 50000
ORDER BY NVL(sstv.total, 0) DESC;

-- Output the quantity of sales by staff members of all products selling over 20000 worth of value
CREATE VIEW TopSellerSalesByStaff AS
SELECT
    s.FName || ' ' || s.LName EmployeeName,
    sub.StaffID StaffID,
    sub.ProductID ProductID,
    -- Get the
    NVL ((
        SELECT SUM(op.ProductQuantity) FROM staff s
        JOIN staff_orders so ON so.StaffID = s.StaffID
        JOIN order_products op ON op.OrderID = so.OrderID
        JOIN inventory i ON i.ProductID = op.ProductID
        WHERE s.StaffID = sub.StaffID AND i.ProductID = sub.ProductID
    ), 0) QuantitySold,
    -- Get the total value of all top sellers the staff member has sold
    NVL ((
        SELECT SUM(op.ProductQuantity * i.ProductPrice) FROM staff s
        JOIN staff_orders so ON so.StaffID = s.StaffID
        JOIN order_products op ON op.OrderID = so.OrderID
        JOIN inventory i ON i.ProductID = op.ProductID
        WHERE s.StaffID = sub.StaffID AND i.ProductID IN (SELECT * FROM TopSellers)
    ), 0) TotalValueSold
FROM (
    -- Get all combinations of StaffID and ProductID
    SELECT
        s.StaffID,
        ts.ProductID
    FROM staff s
    CROSS JOIN TopSellers ts
    JOIN inventory i ON i.ProductID = ts.ProductID
) sub
-- Join inventory for ordering
JOIN inventory i ON i.ProductID = sub.ProductID
JOIN staff s ON s.StaffID = sub.StaffID
ORDER BY TotalValueSold DESC, sub.ProductID;

CREATE VIEW TopSellers AS
SELECT ProductID FROM (
    SELECT i.ProductID, i.ProductPrice, SUM(i.ProductPrice * NVL(op.ProductQuantity, 0)) Value FROM inventory i
    LEFT JOIN order_products op ON op.ProductID = i.ProductID
    GROUP BY i.ProductID, i.ProductPrice
)
WHERE Value > 20000;
