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
    CONSTRAINT PositiveProductID CHECK (ProductID > 0),
    CONSTRAINT PostiiveStock CHECK (ProductStockAmount > 0),
    CONSTRAINT PositivePrice CHECK (ProductPrice > 0)
);

CREATE TABLE orders (
    OrderID INTEGER PRIMARY KEY,
    OrderType VARCHAR(30) NOT NULL,
    OrderCompleted INTEGER NOT NULL,
    OrderPlaced DATE NOT NULL,
    CONSTRAINT PositiveOrderID CHECK (OrderID > 0),
    CONSTRAINT OrderCompletedBoolean CHECK (OrderCompleted >= 0 AND OrderCompleted <= 1),
    CONSTRAINT OrderTypeEnum CHECK (OrderType = 'InStore' OR OrderType = 'Delivery' OR OrderType = 'Collection')
);

CREATE TABLE order_products (
    OrderID INTEGER NOT NULL,
    ProductID INTEGER NOT NULL,
    ProductQuantity INTEGER NOT NULL,
    FOREIGN KEY (OrderID) REFERENCES orders(OrderID),
    FOREIGN KEY (ProductID) REFERENCES inventory(ProductID),
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
    FOREIGN KEY (OrderID) REFERENCES orders(OrderID),
    CONSTRAINT NonEmptyDeliveryFName CHECK (LENGTH(FName) > 0),
    CONSTRAINT NonEmptyDeliveryLName CHECK (LENGTH(LName) > 0),
    CONSTRAINT NonEmptyDeliveryHouse CHECK (LENGTH(House) > 0),
    CONSTRAINT NonEmptyDeliveryStreet CHECK (LENGTH(Street) > 0)
);

CREATE TABLE collections (
    OrderID INTEGER,
    FName VARCHAR(30) NOT NULL,
    LName VARCHAR(30) NOT NULL,
    CollectionDate DATE NOT NULL,
    FOREIGN KEY (OrderID) REFERENCES orders(OrderID),
    CONSTRAINT NonEmptyCollectionFName CHECK (LENGTH(FName) > 0),
    CONSTRAINT NonEmptyCollectionLName CHECK (LENGTH(LName) > 0)
);

CREATE TABLE staff (
    StaffID INTEGER PRIMARY KEY,
    FName VARCHAR(30) NOT NULL,
    LName VARCHAR(30) NOT NULL,
    CONSTRAINT NonEmptyStaffFName CHECK (LENGTH(FName) > 0),
    CONSTRAINT NonEmptyStaffLName CHECK (LENGTH(LName) > 0)
);

CREATE TABLE staff_orders (
    StaffID INTEGER,
    OrderID INTEGER,
    FOREIGN KEY (StaffID) REFERENCES staff(StaffID),
    FOREIGN KEY (OrderID) REFERENCES orders(OrderID)
);

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
SELECT i.ProductID "Product ID",
       i.ProductDesc "Product Description",
       ('£' || NVL(pvs.value, 0)) "Total Value Sold"
FROM ProductValueSold pvs
JOIN inventory i ON i.ProductID = pvs.id
ORDER BY "Total Value Sold" DESC;

-- Package together staff members by instances of products they've sold
CREATE VIEW StaffOrderData AS
SELECT so.StaffID, so.OrderID, op.ProductID, op.ProductQuantity FROM staff_orders so
JOIN orders o ON o.OrderID = so.OrderID
JOIN order_products op ON op.orderID = so.OrderID;

-- Group sales of the same product together
CREATE VIEW StaffSaleProductQuantity AS
SELECT sod.StaffID, sod.ProductID, SUM(sod.ProductQuantity) quant FROM StaffOrderData sod
WHERE (SELECT OrderCompleted FROM orders WHERE OrderID = sod.OrderID) = 1
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
SELECT s.FName || ' ' || s.LName "Employee Name",
       '£' || NVL(sstv.total, 0) "Total"
FROM StaffSaleTotalValue sstv
RIGHT JOIN staff S on s.StaffID = sstv.StaffID
WHERE sstv.total > 50000
ORDER BY NVL(sstv.total, 0) DESC;

-- Output the quantity of sales by staff members of all products selling over 20000 worth of value
CREATE VIEW TopSellerSalesByStaff AS
SELECT s.FName || ' ' || s.LName EmployeeName, sspq.ProductID, sspq.quant FROM StaffSaleProductQuantity sspq
JOIN staff s ON sspq.StaffID = s.StaffID
WHERE sspq.ProductID IN (SELECT pvs.id FROM ProductValueSold pvs WHERE pvs.value > 20000)
ORDER BY sspq.StaffID;

-- Query for option 8
/*
SELECT (s.FName || ' ' || s.LName) "Employee Name", value "Total value sold"
FROM (
    SELECT s.StaffID, SUM(op.ProductQuantity * i.ProductPrice) value FROM staff s
    JOIN staff_orders so ON so.StaffID = s.StaffID
    JOIN orders o ON o.OrderID = so.OrderID
    JOIN order_products op ON op.OrderID = so.OrderID
    JOIN inventory i ON i.ProductID = op.ProductID
    WHERE o.OrderPlaced > TO_DATE('01-Jan-2017') AND
          o.OrderPlaced < TO_DATE('01-Jan-2017') + 365
    GROUP BY s.StaffID
) sub
JOIN staff s ON s.StaffID = sub.StaffID
WHERE value > 50000 AND
      sub.StaffID IN
      (
          SELECT so.StaffID FROM staff_orders so
          JOIN order_products op ON op.OrderID = so.OrderID
          JOIN inventory i ON i.ProductID = op.ProductID
          WHERE i.ProductID IN
          (
              SELECT ProductID FROM (
                  SELECT i.ProductID, SUM(i.ProductPrice * op.ProductQuantity) ValueSold
                  FROM orders o
                  JOIN order_products op ON op.OrderID = o.OrderID
                  JOIN inventory i ON i.ProductID = op.ProductID
                  WHERE o.OrderPlaced > TO_DATE('01-Jan-2017') AND o.OrderPlaced < TO_DATE('01-Jan-2017') + 365
                  GROUP BY i.ProductID
              ) valid
              WHERE ValueSold > 20000
          )
      )
;
*/