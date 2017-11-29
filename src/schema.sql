CREATE TABLE inventory (
    ProductID           INTEGER         PRIMARY KEY,
    ProductDesc         VARCHAR(30),
    ProductPrice        NUMERIC(8, 2)   NOT NULL,
    ProductStockAmount  INTEGER         NOT NULL,

    CHECK (ProductID > 0                AND
           ProductStockAmount > 0       AND
           ProductPrice > 0.0
    )
);

CREATE TABLE orders (
    OrderID             INTEGER         PRIMARY KEY,
    OrderType           VARCHAR(30)     NOT NULL,
    OrderCompleted      INTEGER         NOT NULL,
    OrderPlaced         DATE            NOT NULL,

    CHECK (OrderID > 0                  AND
           OrderCompleted >= 0          AND
           OrderCompleted <= 1
    )
);

CREATE TABLE order_products (
    OrderID             INTEGER         NOT NULL,
    ProductID           INTEGER         NOT NULL,
    ProductQuantity     INTEGER         NOT NULL,

    FOREIGN KEY (OrderID) REFERENCES orders(OrderID),
    FOREIGN KEY (ProductID) REFERENCES inventory(ProductID),

    CHECK (ProductQuantity > 0
    )
);

CREATE TABLE deliveries (
    OrderID             INTEGER         NOT NULL,
    FName               VARCHAR(30)     NOT NULL,
    LName               VARCHAR(30)     NOT NULL,
    House               VARCHAR(30)     NOT NULL,
    Street              VARCHAR(30)     NOT NULL,
    DeliveryDate        DATE            NOT NULL,

    FOREIGN KEY (OrderID) REFERENCES orders(OrderID),

    CHECK (LENGTH(FName) > 0            AND
           LENGTH(LName) > 0            AND
           LENGTH(House) > 0            AND
           LENGTH(Street) > 0
    )
);

CREATE TABLE collections (
    OrderID             INTEGER,
    FName               VARCHAR(30)     NOT NULL,
    LName               VARCHAR(30)     NOT NULL,
    CollectionDate      DATE            NOT NULL,

    FOREIGN KEY (OrderID) REFERENCES orders(OrderID),

    CHECK (LENGTH(FName) > 0            AND
           LENGTH(LName) > 0
    )
);

CREATE TABLE staff (
    StaffID             INTEGER         PRIMARY KEY,
    FName               VARCHAR(30)     NOT NULL,
    LName               VARCHAR(30)     NOT NULL,

    CHECK (StaffID > 0                  AND
           LENGTH(FName) > 0            AND
           LENGTH(LName) > 0
    )
);

CREATE TABLE staff_orders (
    StaffID             INTEGER,
    OrderID             INTEGER,

    FOREIGN KEY (StaffID) REFERENCES staff(StaffID),
    FOREIGN KEY (OrderID) REFERENCES orders(OrderID)
);