-- MySQL dump 10.13  Distrib 8.0.39, for Win64 (x86_64)
--
-- Host: localhost    Database: bankingsystem
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account` (
  `AccountID` int NOT NULL AUTO_INCREMENT,
  `AccountNumber` char(9) NOT NULL,
  `CustomerID` int NOT NULL,
  `AccountType` varchar(20) DEFAULT NULL,
  `Currency` varchar(3) DEFAULT NULL,
  `Balance` decimal(15,2) NOT NULL,
  `InterestRate` decimal(5,2) DEFAULT NULL,
  `Period` int DEFAULT NULL,
  `AccountName` varchar(255) DEFAULT NULL,
  `CreationDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`AccountID`),
  UNIQUE KEY `AccountNumber` (`AccountNumber`),
  KEY `CustomerID` (`CustomerID`),
  CONSTRAINT `account_ibfk_1` FOREIGN KEY (`CustomerID`) REFERENCES `customer` (`CustomerID`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,'983476858',1,'Current Saving','USD',2000.00,0.50,NULL,'CHORY CHANRADY','2024-12-20 19:46:26');
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `accounthistory`
--

DROP TABLE IF EXISTS `accounthistory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `accounthistory` (
  `TransactionID` int NOT NULL AUTO_INCREMENT,
  `SourceAccount` char(9) NOT NULL,
  `SourceName` varchar(255) NOT NULL,
  `DestinationAccount` char(9) DEFAULT NULL,
  `DestinationName` varchar(255) DEFAULT NULL,
  `TransactionType` enum('Deposit','Withdraw','Transfer') NOT NULL,
  `Currency` varchar(50) DEFAULT NULL,
  `Amount` decimal(15,2) NOT NULL,
  `TransactionDate` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`TransactionID`),
  KEY `fk_source_account` (`SourceAccount`),
  KEY `fk_destination_account` (`DestinationAccount`),
  CONSTRAINT `fk_destination_account` FOREIGN KEY (`DestinationAccount`) REFERENCES `account` (`AccountNumber`) ON DELETE SET NULL,
  CONSTRAINT `fk_source_account` FOREIGN KEY (`SourceAccount`) REFERENCES `account` (`AccountNumber`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `accounthistory`
--

LOCK TABLES `accounthistory` WRITE;
/*!40000 ALTER TABLE `accounthistory` DISABLE KEYS */;
INSERT INTO `accounthistory` VALUES (1,'983476858','CHORY CHANRADY',NULL,NULL,'Deposit','KHR',400000.00,'2024-12-20 05:51:04'),(2,'983476858','CHORY CHANRADY',NULL,NULL,'Withdraw','KHR',400000.00,'2024-12-20 05:53:46');
/*!40000 ALTER TABLE `accounthistory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `CustomerID` int NOT NULL AUTO_INCREMENT,
  `CustomerName` varchar(100) NOT NULL,
  `CustomerSex` char(1) DEFAULT NULL,
  `DateOfBirth` date NOT NULL,
  `Nationality` varchar(50) DEFAULT NULL,
  `PlaceOfBirth` varchar(50) DEFAULT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `CurrentAddress` varchar(200) DEFAULT NULL,
  `CreationDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `AssociatedAccountCount` int DEFAULT '0',
  PRIMARY KEY (`CustomerID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` VALUES (1,'CHORY CHANRADY','M','2002-03-11','Khmer','Kompong Cham Province','chory.chanrady@gmail.com','Phnom Penh','2024-12-20 19:45:58',0);
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `deletedaccount`
--

DROP TABLE IF EXISTS `deletedaccount`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `deletedaccount` (
  `AccountID` int NOT NULL AUTO_INCREMENT,
  `AccountNumber` char(9) NOT NULL,
  `CustomerID` int NOT NULL,
  `AccountName` varchar(255) DEFAULT NULL,
  `AccountType` varchar(20) DEFAULT NULL,
  `Currency` varchar(3) DEFAULT NULL,
  `Balance` decimal(15,2) NOT NULL,
  `InterestRate` decimal(5,2) DEFAULT NULL,
  `Period` int DEFAULT NULL,
  `DeletedDate` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `CreationDate` datetime DEFAULT NULL,
  PRIMARY KEY (`AccountID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `deletedaccount`
--

LOCK TABLES `deletedaccount` WRITE;
/*!40000 ALTER TABLE `deletedaccount` DISABLE KEYS */;
/*!40000 ALTER TABLE `deletedaccount` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `deletedcustomer`
--

DROP TABLE IF EXISTS `deletedcustomer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `deletedcustomer` (
  `CustomerID` int NOT NULL AUTO_INCREMENT,
  `CustomerName` varchar(100) NOT NULL,
  `CustomerSex` char(1) DEFAULT NULL,
  `DateOfBirth` date NOT NULL,
  `Nationality` varchar(50) DEFAULT NULL,
  `PlaceOfBirth` varchar(50) DEFAULT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `CurrentAddress` varchar(200) DEFAULT NULL,
  `CreationDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `DeletionDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`CustomerID`),
  CONSTRAINT `deletedcustomer_ibfk_1` FOREIGN KEY (`CustomerID`) REFERENCES `customer` (`CustomerID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `deletedcustomer`
--

LOCK TABLES `deletedcustomer` WRITE;
/*!40000 ALTER TABLE `deletedcustomer` DISABLE KEYS */;
/*!40000 ALTER TABLE `deletedcustomer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `UserID` int NOT NULL,
  `Name` varchar(50) NOT NULL,
  `Password` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `CreationDate` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`UserID`),
  UNIQUE KEY `unique_name` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (61684,'d.admin','Admin@2024','2024-12-20 12:43:22');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-12-20 19:59:31
