ResEasy 🍽️

A Restaurant Table Reservation System with Auto-Release & Waiting List

📌 Overview

ResEasy is a Java-based restaurant table reservation system that allows customers to book tables, manage waiting lists, and automatically release reservations after a specified time. The system also tracks daily revenue and provides an interactive menu-driven interface.

🚀 Features

Table Management:

Supports multiple table sizes (2, 4, 6, 8 seaters).

Ensures customers are allocated to the most suitable table size.

Auto-Release Mechanism:

Reservations expire after the specified time using background threads.

Released tables are reallocated to waiting customers automatically.

Waiting List:

Customers are queued if suitable tables are not available.

Waiting customers are assigned immediately once a table frees up.

Revenue Tracking:

Collects charges based on members × charge per person.

Shows total revenue earned for the day.

Encapsulation & OOP Principles:

Private fields with getters/setters.

Uses Abstract classes and Interfaces (Reservable, AbstractPerson).

Composition: Restaurant has-a list of Tables.

Static usage: Daily revenue tracked as a static variable.
📖 UML Diagram

📋 Example Usage
--- Restaurant Reservation Menu ---
1. View table status
2. Book a table
3. Cancel booking
4. Force-release table
5. View waiting list
6. View total revenue
7. Exit
Choose an option: 2
Enter customer name: Sai
Enter number of members: 2
Enter reservation time (minutes): 3
Enter charge per person: 100
[INFO] Table 1 booked successfully for Sai.
👨‍💻 Author

Developed as part of Java OOP Concepts Project to demonstrate:

Encapsulation

Abstraction

Inheritance

Polymorphism

File Handling

Threads & Concurrency

Static & Composition Usage
