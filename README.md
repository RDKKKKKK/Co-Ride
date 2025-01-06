# **CoRIde - A RideSharing Platform**

This repository contains the codebase for a **RideSharing Platform**, a microservices-based application that facilitates carpooling by connecting drivers with passengers. The platform is designed to support high concurrency, real-time matching, and flexible scheduling, making it ideal for both immediate and scheduled ride requests.

---

## **Features**

### **1. Real-time Ride Matching**
- Efficiently matches drivers and passengers based on proximity, available seats, and departure time.
- Supports **priority-based matching** using custom priority queues.

### **2. Scheduled Rides**
- Allows users to schedule rides for future times.
- The system periodically checks for upcoming rides and matches passengers accordingly.

### **3. High Scalability & Concurrency**
- Utilizes **thread pools** and **synchronized components** to handle high concurrency.
- Implements a custom thread pool with **priority-based task scheduling** to ensure optimal performance under heavy load.

### **4. Messaging & Notifications**
- Real-time notifications sent to users through **WebSocket**.
- Message-driven communication between microservices using **RabbitMQ**.

### **5. Fault Tolerance**
- Designed with **transactional guarantees** and **error handling** to ensure data consistency.
- Uses **Redis** for caching frequently accessed data and managing temporary states like ride confirmations.

### **6. Microservices Architecture**
- The platform follows a microservices architecture with separate services for:
    - **User Management**
    - **Ride Matching**
    - **Gateway Services**
    - **Notification Service**
    