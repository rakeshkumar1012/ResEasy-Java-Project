import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

// Interface for reservation-related actions
interface Reservable {
    void book(String customerName, int members, int minutes, double chargePerPerson, Restaurant restaurant);
    void release();
    void cancel();
}

// Interface for revenue-related actions
interface RevenueTrackable {
    void addRevenue(double amount);
}

// Abstract parent for different kinds of seating
abstract class Seating {
    protected int tableNumber;
    protected int capacity;

    public Seating(int tableNumber, int capacity) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
    }

    public int getTableNumber() { return tableNumber; }
    public int getCapacity() { return capacity; }
}

// Concrete Table class (Encapsulation + Thread + File handling)
class Table extends Seating implements Reservable {
    private boolean booked;
    private String customerName;
    private int members;
    private int reservationMinutes;
    private double chargePerPerson;
    private Thread autoReleaseThread;
    private long startTime;

    public Table(int tableNumber, int capacity) {
        super(tableNumber, capacity);
        this.booked = false;
    }

    public boolean isBooked() { return booked; }
    public String getCustomerName() { return customerName; }
    public int getMembers() { return members; }
    public double getChargePerPerson() { return chargePerPerson; }

    public int getTimeLeft() {
        if (!booked) return 0;
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        long remaining = reservationMinutes * 60L - elapsed;
        return (int) Math.max(remaining, 0);
    }

    // Reservation logic
    @Override
    public void book(String customerName, int members, int minutes, double chargePerPerson, Restaurant restaurant) {
        this.booked = true;
        this.customerName = customerName;
        this.members = members;
        this.reservationMinutes = minutes;
        this.chargePerPerson = chargePerPerson;
        this.startTime = System.currentTimeMillis();

        logAction("BOOKED by " + customerName + " for " + minutes + " min. Members: " + members + ", Charge/person: Rs." + chargePerPerson);

        autoReleaseThread = new Thread(() -> {
            try {
                Thread.sleep(minutes * 60 * 1000L);
                if (this.booked) {
                    release();
                    restaurant.addRevenue(members * chargePerPerson);
                    logAction("AUTO-RELEASED. Collected: Rs." + (members * chargePerPerson));
                    System.out.println("[INFO] Table " + tableNumber + " auto-released. Collected Rs. " + (members * chargePerPerson));
                    restaurant.assignNextWaitingCustomer();
                }
            } catch (InterruptedException e) {
                // interrupted on cancel/force release
            }
        });
        autoReleaseThread.start();
    }

    @Override
    public void release() {
        this.booked = false;
        this.customerName = null;
        this.members = 0;
        this.chargePerPerson = 0;
        this.startTime = 0;
    }

    @Override
    public void cancel() {
        if (autoReleaseThread != null && autoReleaseThread.isAlive()) autoReleaseThread.interrupt();
        release();
    }

    private void logAction(String action) {
        try (FileWriter writer = new FileWriter("reservation_log.csv", true)) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write(tableNumber + "," + time + "," + action + "\n");
        } catch (IOException e) {
            System.out.println("[ERROR] Could not write to log: " + e.getMessage());
        }
    }
}

// Restaurant class (Composition + Static revenue + Nested class)
class Restaurant implements RevenueTrackable {
    private Table[] tables;
    private static double totalRevenue = 0;
    private Queue<WaitingCustomer> waitingList = new LinkedList<>();

    public Restaurant() {
        tables = new Table[5];
        tables[0] = new Table(1, 2);
        tables[1] = new Table(2, 4);
        tables[2] = new Table(3, 6);
        tables[3] = new Table(4, 6);
        tables[4] = new Table(5, 8);
    }

    public void viewTableStatus() {
        System.out.println("\n--- Table Status ---");
        for (Table t : tables) {
            if (t.isBooked()) {
                int timeLeft = t.getTimeLeft();
                System.out.println("Table " + t.getTableNumber() + " (" + t.getCapacity() + "-seater) - BOOKED by "
                        + t.getCustomerName() + " [Time left: " + (timeLeft / 60) + " min " + (timeLeft % 60) + " sec]");
            } else {
                System.out.println("Table " + t.getTableNumber() + " (" + t.getCapacity() + "-seater) - AVAILABLE");
            }
        }
    }

    private int getRequiredSeating(int members) {
        if (members <= 2) return 2;
        if (members <= 4) return 4;
        if (members <= 6) return 6;
        return 8;
    }

    public void bookTable(String customerName, int members, int minutes, double chargePerPerson) {
        int requiredSeating = getRequiredSeating(members);
        Table allocated = null;

        for (Table t : tables) {
            if (!t.isBooked() && t.getCapacity() == requiredSeating) {
                allocated = t;
                break;
            }
        }

        if (allocated != null) {
            allocated.book(customerName, members, minutes, chargePerPerson, this);
            System.out.println("[INFO] Table " + allocated.getTableNumber() + " booked successfully.");
        } else {
            waitingList.add(new WaitingCustomer(customerName, members, minutes, chargePerPerson));
            System.out.println("[INFO] All " + requiredSeating + "-seater tables are full. Added to waiting list.");
        }
    }

    public void cancelBooking(int tableNumber) {
        Table t = getTableByNumber(tableNumber);
        if (t == null) { System.out.println("[ERROR] Table doesn't exist."); return; }
        if (!t.isBooked()) { System.out.println("[ERROR] Table is not booked."); return; }
        t.cancel();
        System.out.println("[INFO] Booking cancelled for Table " + tableNumber);
        assignNextWaitingCustomer();
    }

    public void forceReleaseTable(int tableNumber) {
        Table t = getTableByNumber(tableNumber);
        if (t == null) { System.out.println("[ERROR] Table doesn't exist."); return; }
        if (!t.isBooked()) { System.out.println("[ERROR] Table is already empty."); return; }
        double totalCharge = t.getMembers() * t.getChargePerPerson();
        totalRevenue += totalCharge;
        t.cancel();
        System.out.println("[INFO] Table " + tableNumber + " released. Collected: Rs. " + totalCharge);
        assignNextWaitingCustomer();
    }

    public void assignNextWaitingCustomer() {
        if (waitingList.isEmpty()) return;

        WaitingCustomer next = waitingList.peek();
        int requiredSeating = getRequiredSeating(next.members);

        for (Table t : tables) {
            if (!t.isBooked() && t.getCapacity() == requiredSeating) {
                t.book(next.customerName, next.members, next.minutes, next.chargePerPerson, this);
                waitingList.poll();
                System.out.println("[INFO] Waiting customer " + next.customerName + " assigned Table " + t.getTableNumber());
                break;
            }
        }
    }

    public void viewWaitingList() {
        System.out.println("\n--- Waiting List ---");
        if (waitingList.isEmpty()) System.out.println("No waiting customers.");
        int i = 1;
        for (WaitingCustomer w : waitingList) {
            System.out.println(i + ". " + w.customerName + " (" + w.members + " members)");
            i++;
        }
    }

    private Table getTableByNumber(int tableNumber) {
        for (Table t : tables) if (t.getTableNumber() == tableNumber) return t;
        return null;
    }

    @Override
    public void addRevenue(double amount) {
        totalRevenue += amount;
    }

    public static void showTotalRevenue() {
        System.out.println("Total Revenue Collected Today: Rs. " + totalRevenue);
    }

    // Nested class for waiting list
    private static class WaitingCustomer {
        String customerName;
        int members;
        int minutes;
        double chargePerPerson;

        WaitingCustomer(String customerName, int members, int minutes, double chargePerPerson) {
            this.customerName = customerName;
            this.members = members;
            this.minutes = minutes;
            this.chargePerPerson = chargePerPerson;
        }
    }
}

// Main Application
public class RestaurantReservationApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Restaurant restaurant = new Restaurant();

        while (true) {
            System.out.println("\n--- Restaurant Reservation Menu ---");
            System.out.println("1. View table status");
            System.out.println("2. Book a table");
            System.out.println("3. Cancel booking");
            System.out.println("4. Force-release table");
            System.out.println("5. View waiting list");
            System.out.println("6. View total revenue");
            System.out.println("7. Exit");
            System.out.print("Choose an option: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1: restaurant.viewTableStatus(); break;
                case 2:
                    System.out.print("Enter customer name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter number of members: ");
                    int members = sc.nextInt();
                    System.out.print("Enter reservation time (minutes): ");
                    int mins = sc.nextInt();
                    System.out.print("Enter charge per person: ");
                    double charge = sc.nextDouble();
                    restaurant.bookTable(name, members, mins, charge);
                    break;
                case 3:
                    System.out.print("Enter table number to cancel: ");
                    int cancelNum = sc.nextInt();
                    restaurant.cancelBooking(cancelNum);
                    break;
                case 4:
                    System.out.print("Enter table number to force-release: ");
                    int releaseNum = sc.nextInt();
                    restaurant.forceReleaseTable(releaseNum);
                    break;
                case 5:
                    restaurant.viewWaitingList();
                    break;
                case 6:
                    Restaurant.showTotalRevenue();
                    break;
                case 7:
                    System.out.println("Exiting...");
                    sc.close();
                    return;
                default:
                    System.out.println("[ERROR] Invalid option.");
            }
        }
    }
}
