CREATE TABLE inventory (
    ProductID INTEGER PRIMARY KEY AUTO_INCREMENT,
    ProductDesc VARCHAR(30),
    ProductPrice NUMERIC(8, 2) NOT NULL,
    ProductStockAmount INTEGER NOT NULL,
    CONSTRAINT PositiveProductID CHECK (ProductID > 0),
    CONSTRAINT PostiiveStock CHECK (ProductStockAmount > 0),
    CONSTRAINT PositivePrice CHECK (ProductPrice > 0)
);
CREATE TABLE orders (
    OrderID INTEGER PRIMARY KEY AUTO_INCREMENT,
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
    CONSTRAINT PositiveQuantity (ProductQuantity > 0)
);
CREATE TABLE deliveries (
    OrderID INTEGER NOT NULL,
    FName VARCHAR(30) NOT NULL,
    LName VARCHAR(30) NOT NULL,
    House VARCHAR(30) NOT NULL,
    Street VARCHAR(30) NOT NULL,
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
    StaffID INTEGER PRIMARY KEY AUTO_INCREMENT,
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