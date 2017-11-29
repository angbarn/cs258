CREATE SEQUENCE pk_staff
START WITH 1
INCREMENT BY 1;

CREATE SEQUENCE pk_order
START WITH 1
INCREMENT BY 1;

CREATE SEQUENCE pk_inventory
START WITH 1
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

CREATE OR REPLACE TRIGGER new_staff_member_id
BEFORE INSERT ON staff
    FOR EACH ROW BEGIN
        :new.StaffID := pk_staff.nextval;
    END;
/

CREATE OR REPLACE TRIGGER new_order_id
BEFORE INSERT ON orders
    FOR EACH ROW BEGIN
        :new.OrderID := pk_order.nextval;
    END;
/

CREATE OR REPLACE TRIGGER new_product_id
BEFORE INSERT ON inventory
    FOR EACH ROW BEGIN
        :new.ProductID := pk_inventory.nextval;
    END;
/

-- For option 4
CREATE VIEW ProductValueSold AS
SELECT ProductID, ProductDesc, Sold
FROM (SELECT ProductID,
             ProductDesc,
             (SELECT NVL(SUM(ProductQuantity), 0.0) * ProductPrice FROM order_products op WHERE ProductID = i.ProductID) AS Sold
      FROM inventory i)
ORDER BY Sold DESC;


/*
-- For option 4, but it discounts incomplete orders
CREATE VIEW ProductValueSold AS
SELECT ProductID, ProductDesc, Sold
FROM (SELECT ProductID,
             ProductDesc,
             (SELECT NVL(SUM(ProductQuantity), 0.0) * ProductPrice FROM order_products op JOIN orders o ON o.OrderID = op.OrderID WHERE ProductID = i.ProductID AND o.OrderCompleted = 1) AS Sold
      FROM inventory i)
ORDER BY Sold DESC;
*/
