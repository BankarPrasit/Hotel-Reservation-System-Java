import java.io.*;
import java.util.*;

/**
 * TASK 2: Hotel Reservation System
 * Uses File I/O to persist rooms and bookings (CSV files created automatically).
 * Compile: javac HotelReservationSystem.java
 * Run:     java HotelReservationSystem
 */
public class HotelReservationSystem {

    static final String ROOMS_FILE = "rooms.csv";
    static final String BOOKINGS_FILE = "bookings.csv";

    // ---------- Room class ----------
    static class Room {
        int roomNo;
        String category; // Standard, Deluxe, Suite
        double pricePerNight;
        boolean isBooked;

        Room(int roomNo, String category, double pricePerNight, boolean isBooked) {
            this.roomNo = roomNo;
            this.category = category;
            this.pricePerNight = pricePerNight;
            this.isBooked = isBooked;
        }

        String toCsv() {
            return roomNo + "," + category + "," + pricePerNight + "," + isBooked;
        }

        static Room fromCsv(String line) {
            String[] p = line.split(",");
            return new Room(Integer.parseInt(p[0]), p[1], Double.parseDouble(p[2]), Boolean.parseBoolean(p[3]));
        }
    }

    // ---------- Booking class ----------
    static class Booking {
        int bookingId;
        String guestName;
        int roomNo;
        int nights;
        double totalAmount;
        String paymentStatus; // PAID / CANCELLED

        Booking(int bookingId, String guestName, int roomNo, int nights, double totalAmount, String paymentStatus) {
            this.bookingId = bookingId;
            this.guestName = guestName;
            this.roomNo = roomNo;
            this.nights = nights;
            this.totalAmount = totalAmount;
            this.paymentStatus = paymentStatus;
        }

        String toCsv() {
            return bookingId + "," + guestName + "," + roomNo + "," + nights + "," + totalAmount + "," + paymentStatus;
        }

        static Booking fromCsv(String line) {
            String[] p = line.split(",");
            return new Booking(Integer.parseInt(p[0]), p[1], Integer.parseInt(p[2]),
                    Integer.parseInt(p[3]), Double.parseDouble(p[4]), p[5]);
        }
    }

    static ArrayList<Room> rooms = new ArrayList<>();
    static ArrayList<Booking> bookings = new ArrayList<>();
    static Scanner sc = new Scanner(System.in);
    static int nextBookingId = 1;

    public static void main(String[] args) {
        loadRooms();
        loadBookings();

        int choice;
        do {
            printMenu();
            choice = readInt("Enter choice: ");
           switch (choice) {
    case 1:
        viewAvailableRooms();
        break;

    case 2:
        bookRoom();
        break;

    case 3:
        cancelReservation();
        break;

    case 4:
        viewAllBookings();
        break;

    case 5:
        viewRoomsByCategory();
        break;

    case 6:
        System.out.println("Exiting... Goodbye!");
        break;

    default:
        System.out.println("Invalid choice. Try again.");
}
        } while (choice != 6);

        saveRooms();
        saveBookings();
        sc.close();
    }

    static void printMenu() {
        System.out.println("\n===== HOTEL RESERVATION SYSTEM =====");
        System.out.println("1. View Available Rooms");
        System.out.println("2. Book a Room");
        System.out.println("3. Cancel a Reservation");
        System.out.println("4. View All Bookings");
        System.out.println("5. View Rooms by Category");
        System.out.println("6. Exit");
    }

    // ---------- Initial data (only created first run) ----------
    static void initializeDefaultRooms() {
        rooms.add(new Room(101, "Standard", 1500, false));
        rooms.add(new Room(102, "Standard", 1500, false));
        rooms.add(new Room(103, "Standard", 1500, false));
        rooms.add(new Room(201, "Deluxe", 2800, false));
        rooms.add(new Room(202, "Deluxe", 2800, false));
        rooms.add(new Room(301, "Suite", 5000, false));
        rooms.add(new Room(302, "Suite", 5500, false));
        saveRooms();
        System.out.println("Default room inventory created (rooms.csv).");
    }

    // ---------- Core operations ----------
    static void viewAvailableRooms() {
        System.out.println("\n--- Available Rooms ---");
        System.out.printf("%-8s %-10s %-12s%n", "Room No", "Category", "Price/Night");
        boolean any = false;
        for (Room r : rooms) {
            if (!r.isBooked) {
                System.out.printf("%-8d %-10s Rs%-11.2f%n", r.roomNo, r.category, r.pricePerNight);
                any = true;
            }
        }
        if (!any) System.out.println("No rooms currently available.");
    }

    static void viewRoomsByCategory() {
        System.out.print("Enter category (Standard/Deluxe/Suite): ");
        String cat = sc.nextLine().trim();
        System.out.println("\n--- " + cat + " Rooms ---");
        System.out.printf("%-8s %-12s %-10s%n", "Room No", "Price/Night", "Status");
        boolean any = false;
        for (Room r : rooms) {
            if (r.category.equalsIgnoreCase(cat)) {
                System.out.printf("%-8d Rs%-11.2f %-10s%n", r.roomNo, r.pricePerNight,
                        r.isBooked ? "Booked" : "Available");
                any = true;
            }
        }
        if (!any) System.out.println("No rooms found in this category.");
    }

    static void bookRoom() {
        viewAvailableRooms();
        int roomNo = readInt("\nEnter room number to book: ");

        Room selected = null;
        for (Room r : rooms) {
            if (r.roomNo == roomNo) {
                selected = r;
                break;
            }
        }

        if (selected == null) {
            System.out.println("[ERROR] Room not found.");
            return;
        }
        if (selected.isBooked) {
            System.out.println("[ERROR] Room already booked.");
            return;
        }

        System.out.print("Enter guest name: ");
        String guestName = sc.nextLine();
        int nights = readInt("Enter number of nights: ");

        double total = selected.pricePerNight * nights;

        System.out.printf("Total amount payable: Rs%.2f%n", total);
        boolean paid = simulatePayment(total);

        if (paid) {
            selected.isBooked = true;
            Booking b = new Booking(nextBookingId++, guestName, selected.roomNo, nights, total, "PAID");
            bookings.add(b);
            saveRooms();
            saveBookings();
            System.out.println("[SUCCESS] Booking confirmed!");
            printBookingDetails(b, selected);
        } else {
            System.out.println("[ERROR] Payment failed. Booking not completed.");
        }
    }

    static boolean simulatePayment(double amount) {
        System.out.println("\n--- Payment Simulation ---");
        System.out.println("1. Credit/Debit Card");
        System.out.println("2. UPI");
        System.out.println("3. Cash");
        int method = readInt("Select payment method: ");
        if (method < 1 || method > 3) {
            System.out.println("Invalid payment method.");
            return false;
        }
        System.out.printf("Processing payment of Rs%.2f...%n", amount);
        System.out.println("[SUCCESS] Payment successful.");
        return true;
    }

    static void printBookingDetails(Booking b, Room r) {
        System.out.println("\n--- Booking Receipt ---");
        System.out.println("Booking ID  : " + b.bookingId);
        System.out.println("Guest Name  : " + b.guestName);
        System.out.println("Room No     : " + b.roomNo + " (" + r.category + ")");
        System.out.println("Nights      : " + b.nights);
        System.out.printf("Total Paid  : Rs%.2f%n", b.totalAmount);
        System.out.println("Status      : " + b.paymentStatus);
    }

    static void cancelReservation() {
        int bookingId = readInt("Enter booking ID to cancel: ");
        Booking target = null;
        for (Booking b : bookings) {
            if (b.bookingId == bookingId && b.paymentStatus.equals("PAID")) {
                target = b;
                break;
            }
        }

        if (target == null) {
            System.out.println("[ERROR] Active booking with this ID not found.");
            return;
        }

        for (Room r : rooms) {
            if (r.roomNo == target.roomNo) {
                r.isBooked = false;
                break;
            }
        }

        target.paymentStatus = "CANCELLED";
        saveRooms();
        saveBookings();
        System.out.println("[SUCCESS] Booking #" + bookingId + " cancelled. Refund simulated. Room is now available.");
    }

    static void viewAllBookings() {
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        System.out.println("\n--- All Bookings ---");
        System.out.printf("%-5s %-15s %-8s %-8s %-12s %-10s%n",
                "ID", "Guest", "Room", "Nights", "Total", "Status");
        for (Booking b : bookings) {
            System.out.printf("%-5d %-15s %-8d %-8d Rs%-11.2f %-10s%n",
                    b.bookingId, b.guestName, b.roomNo, b.nights, b.totalAmount, b.paymentStatus);
        }
    }

    // ---------- File I/O ----------
    static void loadRooms() {
        File f = new File(ROOMS_FILE);
        if (!f.exists()) {
            initializeDefaultRooms();
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) rooms.add(Room.fromCsv(line));
            }
        } catch (IOException e) {
            System.out.println("Error loading rooms: " + e.getMessage());
        }
    }

    static void saveRooms() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ROOMS_FILE))) {
            for (Room r : rooms) pw.println(r.toCsv());
        } catch (IOException e) {
            System.out.println("Error saving rooms: " + e.getMessage());
        }
    }

    static void loadBookings() {
        File f = new File(BOOKINGS_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            int maxId = 0;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Booking b = Booking.fromCsv(line);
                    bookings.add(b);
                    if (b.bookingId > maxId) maxId = b.bookingId;
                }
            }
            nextBookingId = maxId + 1;
        } catch (IOException e) {
            System.out.println("Error loading bookings: " + e.getMessage());
        }
    }

    static void saveBookings() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(BOOKINGS_FILE))) {
            for (Booking b : bookings) pw.println(b.toCsv());
        } catch (IOException e) {
            System.out.println("Error saving bookings: " + e.getMessage());
        }
    }

    // ---------- Input helper ----------
    static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }
}