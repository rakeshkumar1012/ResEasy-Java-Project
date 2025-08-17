# 🍴 ResEasy – Restaurant Reservation System  

---

## 📌 Overview  
**ResEasy** is a Java-based restaurant reservation system that allows customers to book tables, manage waiting lists, and automatically release reservations after a specified time. It provides an interactive menu-driven interface, ensuring efficient table allocation and revenue tracking for restaurants.  

---

## 🚀 Features  

✅ **Table Management** – Supports multiple table sizes (2, 4, 6, 8 seaters).  
✅ **Smart Allocation** – Customers are assigned to the most suitable table.  
✅ **Auto-Release** – Reservations expire automatically after set duration.  
✅ **Waiting List** – Customers are queued and auto-assigned when tables free up.  
✅ **Revenue Tracking** – Calculates daily revenue from reservations.  
✅ **OOP Principles** – Encapsulation, Abstraction, Inheritance, Interfaces.  
✅ **Threading** – Background threads handle reservation expiry.  

---

## 📖 UML Diagram  

```
+--------------------+         1        *  +--------------------+
|    Restaurant      |-------------------->|       Table        |
+--------------------+                     +--------------------+
| - tables: List<Table>                    | - tableNumber: int |
| - waitingList: Queue<Customer>           | - capacity: int    |
| - totalRevenue: double (static)          | - booked: boolean  |
+--------------------+                     | - customerName: String |
| + viewTables()                          | - members: int     |
| + bookTable(Customer, members, ...)     | - reservationTime: int |
| + cancelBooking(tableNo)                | - chargePerPerson: double |
| + forceRelease(tableNo)                 | - autoReleaseThread: Thread |
| + viewWaitingList()                     +--------------------+
| + viewRevenue()                         | + book(...)        |
|                                          | + release()        |
|                                          | + getDetails()     |
+--------------------+                     +--------------------+
         *
         |
         v
+--------------------+         implements
|     Customer       |--------------------+
+--------------------+                    |
| - name: String     |                    |
| - members: int     |                    |
+--------------------+                    |
| + getName()        |                    |
| + getMembers()     |                    |
+--------------------+                    |
                                          v
                                  +--------------------+
                                  |   AbstractPerson   | <<abstract>>
                                  +--------------------+
                                  | - name: String     |
                                  | - members: int     |
                                  +--------------------+
                                  | + getName()        |
                                  | + getMembers()     |
                                  +--------------------+

+--------------------+  <<interface>>
|     Reservable     |
+--------------------+
| + book(...)        |
| + release()        |
+--------------------+
```

---
## 🛠️ How to Run  

1️⃣ Clone or download the project.  
2️⃣ Open terminal and compile:  
```sh
javac RestaurantReservationApp.java
```  
3️⃣ Run the program:  
```sh
java RestaurantReservationApp
```  

---

## 📋 Example Usage  

```
--- Restaurant Reservation Menu ---
1. View table status
2. Book a table
3. Cancel booking
4. Force-release table
5. View waiting list
6. View total revenue
7. Exit

Choose an option: 2
Enter customer name: Suriii
Enter number of members: 3
Enter reservation time (minutes): 2
Enter charge per person: 120
[INFO] Table 2 booked successfully for Suriii (Expires in 2 minutes).
```

---

## 👨‍💻 Author  

Developed as part of a **Java OOP Project** demonstrating:  
- Encapsulation  
- Abstraction  
- Inheritance  
- Polymorphism  
- Interfaces  
- Static & Composition  
- Threads and Concurrency  

---

✨ **ResEasy – Making Reservations Easy!**  
