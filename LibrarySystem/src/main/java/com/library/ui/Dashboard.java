package com.library.ui;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Reservation;
import com.library.model.User;
import com.library.service.*;

import java.sql.SQLException;
import java.util.List;

public class Dashboard {

    private final AuthService        authService;
    private final BookService        bookService        = new BookService();
    private final LoanService        loanService        = new LoanService();
    private final ReservationService reservationService = new ReservationService();

    public Dashboard(AuthService authService) {
        this.authService = authService;
    }

    // -----------------------------------------------------------------------
    // Main dashboard loop
    // -----------------------------------------------------------------------

    public void show() {
        User user = authService.getCurrentUser();
        boolean running = true;
        while (running) {
            printDashboard(user);
            String choice = ConsoleUI.readLine("  Enter choice: ");
            switch (choice) {
                case "1"  -> searchBooks();
                case "2"  -> viewBookDetails();
                case "3"  -> browseCategories();
                case "4"  -> reserveBooks();
                case "5"  -> borrowBooks();
                case "6"  -> returnBooks();
                case "7"  -> viewRentals();
                case "8"  -> viewReservationStatus();
                case "9"  -> viewBorrowingHistory();
                case "10" -> viewReturnedBooks();
                case "11" -> { authService.logout(); running = false; }
                default   -> ConsoleUI.error("Invalid choice. Please enter 1-11.");
            }
        }
        System.out.println("\n  You have been logged out. Goodbye!");
    }

    private void printDashboard(User user) {
        System.out.println("\n" + ConsoleUI.LINE_62);
        System.out.printf("  Welcome! %s  [%s]%n",
            user.getDisplayName(), user.getMemberId());
        System.out.println(ConsoleUI.LINE_62);
        System.out.println();
        System.out.println("  MENU:");
        System.out.println("    [1]  Search Books");
        System.out.println("    [2]  View Book Details");
        System.out.println("    [3]  Browse Book Categories");
        System.out.println("    [4]  Reserve Books");
        System.out.println("    [5]  Borrow Books");
        System.out.println("    [6]  Return Books");
        System.out.println("    [7]  View Book Rentals");
        System.out.println("    [8]  View Reservation Status");
        System.out.println("    [9]  View Borrowing History");
        System.out.println("    [10] View Returned Books");
        System.out.println("    [11] Log Out");
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // [1] Search Books
    // -----------------------------------------------------------------------

    private void searchBooks() {
        while (true) {
            ConsoleUI.printHeader("SEARCH BOOKS");
            System.out.println();
            System.out.println("  Search by:");
            System.out.println("    [1] Title");
            System.out.println("    [2] Author Name");
            System.out.println("    [3] Back to Dashboard");
            System.out.println();

            String choice = ConsoleUI.readLine("  Enter choice: ");

            if (choice.equals("3")) return;
            if (!choice.equals("1") && !choice.equals("2")) {
                ConsoleUI.error("Invalid choice."); continue;
            }

            String label   = choice.equals("1") ? "title" : "author";
            String keyword = ConsoleUI.readLine("  Enter keyword: ");

            try {
                List<Book> books = choice.equals("1")
                    ? bookService.searchByTitle(keyword)
                    : bookService.searchByAuthor(keyword);

                System.out.println();
                System.out.printf("  Searching catalog...%n");

                if (books.isEmpty()) {
                    System.out.printf("  0 result(s) found for: '%s'%n", keyword);
                    ConsoleUI.pressEnter();
                    continue;
                }

                System.out.printf("  %d result(s) found for: '%s'%n%n", books.size(), keyword);
                printBookTable(books);

                String sel = ConsoleUI.readLine("\n  Enter item # to view details [0] Back\n  Enter choice: ");
                if (!sel.equals("0")) {
                    try {
                        int idx = Integer.parseInt(sel) - 1;
                        if (idx >= 0 && idx < books.size()) {
                            showBookDetail(books.get(idx));
                        } else {
                            ConsoleUI.error("Invalid selection.");
                        }
                    } catch (NumberFormatException e) {
                        ConsoleUI.error("Invalid input.");
                    }
                }
            } catch (SQLException e) {
                ConsoleUI.error("Database error: " + e.getMessage());
                ConsoleUI.pressEnter();
            }
        }
    }

    private void printBookTable(List<Book> books) {
        System.out.println("  " + ConsoleUI.DASH_62);
        System.out.printf("  %-4s | %-32s | %-14s | %-12s%n",
            "#", "Title", "Author", "Status");
        System.out.println("  " + ConsoleUI.DASH_62);
        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            String author = b.getAuthor().length() > 14
                ? b.getAuthor().substring(0, 12) + ".." : b.getAuthor();
            System.out.printf("  %-4d | %-32s | %-14s | %-12s%n",
                i + 1,
                ConsoleUI.col(b.getTitle(), 32),
                author,
                b.getStatus());
        }
        System.out.println("  " + ConsoleUI.DASH_62);
    }

    // -----------------------------------------------------------------------
    // [2] View Book Details
    // -----------------------------------------------------------------------

    private void viewBookDetails() {
        ConsoleUI.printHeader("BOOK DETAILS");
        System.out.println();
        String bookIdInput = ConsoleUI.readLine("  Enter Book ID (e.g. BOOK-0001): ");

        try {
            Book book = bookService.findByBookId(bookIdInput);
            if (book == null) {
                ConsoleUI.error("Book not found with ID: " + bookIdInput);
                ConsoleUI.pressEnter();
                return;
            }
            showBookDetail(book);
        } catch (SQLException e) {
            ConsoleUI.error("Database error: " + e.getMessage());
            ConsoleUI.pressEnter();
        }
    }

    private void showBookDetail(Book book) {
        System.out.println("\n" + ConsoleUI.LINE_62);
        System.out.println("  BOOK DETAILS");
        System.out.println(ConsoleUI.LINE_62);
        System.out.println();
        System.out.printf("  Book ID        : %s%n", book.getBookId());
        System.out.printf("  Title          : %s%n", book.getTitle());
        System.out.printf("  Author         : %s%n", book.getAuthor());
        System.out.printf("  Genre          : %s%n", book.getGenre());
        System.out.printf("  Total Copies   : %d%n", book.getTotalCopies());
        System.out.printf("  Available      : %d%n", book.getAvailableCopies());
        System.out.printf("  Status         : %s%n", book.getStatus());
        System.out.println("\n  " + ConsoleUI.DASH_62);
        ConsoleUI.pressEnter();
    }

    // -----------------------------------------------------------------------
    // [3] Browse Book Categories
    // -----------------------------------------------------------------------

    private void browseCategories() {
        while (true) {
            ConsoleUI.printHeader("BROWSE BOOK CATEGORIES");
            System.out.println();
            System.out.println("  Select a Category:");
            System.out.println();
            for (int i = 0; i < BookService.CATEGORIES.length; i++) {
                System.out.printf("    [%d] %s%n", i + 1, BookService.CATEGORIES[i]);
            }
            System.out.printf("    [%d] Back to Dashboard%n", BookService.CATEGORIES.length + 1);
            System.out.println();

            String choice = ConsoleUI.readLine("  Enter choice: ");
            try {
                int idx = Integer.parseInt(choice) - 1;
                if (idx == BookService.CATEGORIES.length) return;
                if (idx < 0 || idx >= BookService.CATEGORIES.length) {
                    ConsoleUI.error("Invalid choice."); continue;
                }

                String genre = BookService.CATEGORIES[idx];
                List<Book> books = bookService.browseByGenre(genre);

                System.out.printf("%n  Category: %s%n%n", genre);
                if (books.isEmpty()) {
                    System.out.println("  No books found in this category.");
                } else {
                    System.out.println("  " + ConsoleUI.DASH_62);
                    System.out.printf("  %-4s | %-36s | %-12s%n", "#", "Title", "Status");
                    System.out.println("  " + ConsoleUI.DASH_62);
                    for (int i = 0; i < books.size(); i++) {
                        Book b = books.get(i);
                        System.out.printf("  %-4d | %-36s | %-12s%n",
                            i + 1, ConsoleUI.col(b.getTitle(), 36), b.getStatus());
                    }
                    System.out.println("  " + ConsoleUI.DASH_62);

                    String sel = ConsoleUI.readLine("\n  Enter item # to view details [0] Back\n  Enter choice: ");
                    if (!sel.equals("0")) {
                        try {
                            int bookIdx = Integer.parseInt(sel) - 1;
                            if (bookIdx >= 0 && bookIdx < books.size()) showBookDetail(books.get(bookIdx));
                            else ConsoleUI.error("Invalid selection.");
                        } catch (NumberFormatException ignored) {}
                    }
                }
                ConsoleUI.pressEnter();

            } catch (NumberFormatException e) {
                ConsoleUI.error("Invalid input.");
            } catch (SQLException e) {
                ConsoleUI.error("Database error: " + e.getMessage());
                ConsoleUI.pressEnter();
            }
        }
    }

    // -----------------------------------------------------------------------
    // [4] Reserve Books
    // -----------------------------------------------------------------------

    private void reserveBooks() {
        ConsoleUI.printHeader("RESERVE BOOKS");
        System.out.println();
        String bookIdInput = ConsoleUI.readLine("  Enter Book ID (e.g. BOOK-0001): ");

        try {
            Book book = bookService.findByBookId(bookIdInput);
            if (book == null) {
                ConsoleUI.error("Book not found with ID: " + bookIdInput);
                ConsoleUI.pressEnter();
                return;
            }

            System.out.printf("%n  Selected: %s%n%n", book.getTitle());
            String confirm = ConsoleUI.readConfirm("  Confirm reservation for this book? [Y/N]: ");
            if (confirm.equals("N")) {
                ConsoleUI.info("Reservation cancelled."); ConsoleUI.pressEnter(); return;
            }

            System.out.println("\n  Processing reservation...");

            int userId = authService.getCurrentUser().getId();
            ReservationService.ReserveResult result =
                reservationService.reserveBook(userId, book.getId());

            switch (result) {
                case SUCCESS -> {
                    ConsoleUI.success("Reservation placed!");
                    if (reservationService.hasAvailableCopies(book.getId())) {
                        System.out.println();
                        ConsoleUI.notice("Copies are available. Proceed to Borrow instead.");
                        System.out.println();
                        System.out.println("    [1] Borrow Now");
                        System.out.println("    [2] Reserve Anyway (keep reservation)");
                        System.out.println("    [3] Back");
                        System.out.println();
                        String sub = ConsoleUI.readLine("  Enter choice: ");
                        if (sub.equals("1")) borrowByBook(book);
                    }
                }
                case ALREADY_RESERVED -> ConsoleUI.error("You already have an active reservation for this book.");
                case BOOK_NOT_FOUND   -> ConsoleUI.error("Book not found.");
                default               -> ConsoleUI.error("An error occurred.");
            }

            ConsoleUI.pressEnter();

        } catch (SQLException e) {
            ConsoleUI.error("Database error: " + e.getMessage());
            ConsoleUI.pressEnter();
        }
    }

    // -----------------------------------------------------------------------
    // [5] Borrow Books
    // -----------------------------------------------------------------------

    private void borrowBooks() {
        ConsoleUI.printHeader("BORROW BOOKS");
        System.out.println();
        String bookIdInput = ConsoleUI.readLine("  Enter Book ID (e.g. BOOK-0001): ");
        try {
            Book book = bookService.findByBookId(bookIdInput);
            if (book == null) {
                ConsoleUI.error("Book not found with ID: " + bookIdInput);
                ConsoleUI.pressEnter(); return;
            }
            borrowByBook(book);
        } catch (SQLException e) {
            ConsoleUI.error("Database error: " + e.getMessage());
            ConsoleUI.pressEnter();
        }
    }

    private void borrowByBook(Book book) throws SQLException {
        System.out.println();
        System.out.printf("  Book Found: %s%n", book.getTitle());
        System.out.printf("  Available Copies : %d%n", book.getAvailableCopies());

        if (book.getAvailableCopies() <= 0) {
            ConsoleUI.error("No copies available. Please reserve this book.");
            ConsoleUI.pressEnter(); return;
        }

        String dueDate = java.time.LocalDate.now().plusDays(14).toString();
        System.out.printf("  Due Date         : %s%n%n", dueDate);

        String confirm = ConsoleUI.readConfirm("  Confirm borrow? [Y/N]: ");
        if (confirm.equals("N")) { ConsoleUI.info("Borrow cancelled."); ConsoleUI.pressEnter(); return; }

        System.out.println("\n  Processing checkout...");

        int userId = authService.getCurrentUser().getId();
        LoanService.BorrowResult result = loanService.borrowBook(userId, book.getBookId());

        switch (result) {
            case SUCCESS -> {
                Loan loan = loanService.getLastLoan(userId, book.getBookId());
                ConsoleUI.success("Book checked out!");
                if (loan != null) {
                    System.out.printf("  Due Date  : %s%n", loan.getDueDate());
                    System.out.printf("  Loan ID   : %s%n", loan.getLoanId());
                }
            }
            case NO_COPIES        -> ConsoleUI.error("No copies available.");
            case MAX_LOANS_REACHED-> ConsoleUI.error("You have reached the maximum of 5 active loans.");
            case ALREADY_BORROWED -> ConsoleUI.error("You already have an active loan for this book.");
            default               -> ConsoleUI.error("An error occurred.");
        }
        ConsoleUI.pressEnter();
    }

    // -----------------------------------------------------------------------
    // [6] Return Books
    // -----------------------------------------------------------------------

    private void returnBooks() {
        ConsoleUI.printHeader("RETURN BOOKS");
        System.out.println();
        String loanId = ConsoleUI.readLine("  Enter Loan ID (e.g. LOAN-0001): ");

        try {
            Loan loan = loanService.findByLoanId(loanId);
            if (loan == null) {
                ConsoleUI.error("Loan not found with ID: " + loanId);
                ConsoleUI.pressEnter(); return;
            }
            if (!loan.getStatus().equals("Active")) {
                ConsoleUI.error("This loan has already been returned.");
                ConsoleUI.pressEnter(); return;
            }
            if (loan.getUserId() != authService.getCurrentUser().getId()) {
                ConsoleUI.error("This loan does not belong to your account.");
                ConsoleUI.pressEnter(); return;
            }

            System.out.println();
            System.out.printf("  Book     : %s%n", loan.getBookTitle());
            System.out.printf("  Borrowed : %s%n", loan.getBorrowedDate());
            System.out.printf("  Due Date : %s%n", loan.getDueDate());
            System.out.printf("  Returning: %s%n%n", java.time.LocalDate.now());

            String confirm = ConsoleUI.readConfirm("  Confirm return? [Y/N]: ");
            if (confirm.equals("N")) { ConsoleUI.info("Return cancelled."); ConsoleUI.pressEnter(); return; }

            int userId = authService.getCurrentUser().getId();
            LoanService.ReturnResult result = loanService.returnBook(userId, loanId);

            if (result == LoanService.ReturnResult.SUCCESS) {
                String returnId = loanService.getReturnId(loanId);
                ConsoleUI.success("Book returned successfully.");
                System.out.printf("  Return ID : %s%n", returnId);

                java.time.LocalDate due  = java.time.LocalDate.parse(loan.getDueDate());
                java.time.LocalDate today= java.time.LocalDate.now();
                long diff = java.time.temporal.ChronoUnit.DAYS.between(today, due);
                if (diff >= 0)
                    ConsoleUI.info(diff == 0 ? "Returned on time!" : "Thank you for returning " + diff + " day(s) early!");
                else
                    ConsoleUI.info("Book was " + Math.abs(diff) + " day(s) overdue.");
            } else {
                ConsoleUI.error("Failed to process return. Please try again.");
            }
            ConsoleUI.pressEnter();

        } catch (SQLException e) {
            ConsoleUI.error("Database error: " + e.getMessage());
            ConsoleUI.pressEnter();
        }
    }

    // -----------------------------------------------------------------------
    // [7] View Book Rentals
    // -----------------------------------------------------------------------

    private void viewRentals() {
        User user = authService.getCurrentUser();

        try {
            List<Loan> loans = loanService.getActiveLoans(user.getId());

            ConsoleUI.printHeader("VIEW BOOK RENTALS");
            System.out.println();
            System.out.printf("  Member: %s [%s]%n", user.getDisplayName(), user.getMemberId());
            System.out.printf("  Active Rentals: %d%n%n", loans.size());

            if (loans.isEmpty()) {
                System.out.println("  You have no active rentals.");
            } else {
                System.out.println("  " + ConsoleUI.DASH_62);
                System.out.printf("  %-4s | %-30s | %-12s | %-10s | %-10s%n",
                    "#", "Title", "Loan ID", "Due Date", "Status");
                System.out.println("  " + ConsoleUI.DASH_62);
                for (int i = 0; i < loans.size(); i++) {
                    Loan l = loans.get(i);
                    System.out.printf("  %-4d | %-30s | %-10s | %-10s | %-10s%n",
                        i + 1,
                        ConsoleUI.col(l.getBookTitle(), 30),
                        l.getLoanId(),
                        l.getDueDate(),
                        l.getDueSoonStatus());
                }
                System.out.println("  " + ConsoleUI.DASH_62);
            }

            System.out.println();
            System.out.println("  ACTIONS:");
            System.out.println("    [1] Renew a Loan");
            System.out.println("    [2] Return a Book");
            System.out.println("    [3] Back to Dashboard");
            System.out.println();

            String choice = ConsoleUI.readLine("  Enter choice: ");
            switch (choice) {
                case "1" -> renewLoan(loans);
                case "2" -> returnBooks();
                case "3" -> {}
                default  -> ConsoleUI.error("Invalid choice.");
            }

        } catch (SQLException e) {
            ConsoleUI.error("Database error: " + e.getMessage());
            ConsoleUI.pressEnter();
        }
    }

    private void renewLoan(List<Loan> loans) {
        if (loans.isEmpty()) { ConsoleUI.error("No active loans to renew."); ConsoleUI.pressEnter(); return; }
        System.out.println();
        String loanId = ConsoleUI.readLine("  Enter Loan ID to renew: ");
        try {
            boolean ok = loanService.renewLoan(loanId, authService.getCurrentUser().getId());
            if (ok) {
                Loan updated = loanService.findByLoanId(loanId);
                ConsoleUI.success("Loan renewed successfully!");
                if (updated != null) System.out.printf("  New Due Date: %s%n", updated.getDueDate());
            } else {
                ConsoleUI.error("Could not renew loan. Check the Loan ID or ensure it's your active loan.");
            }
        } catch (SQLException e) {
            ConsoleUI.error("Database error: " + e.getMessage());
        }
        ConsoleUI.pressEnter();
    }

    // -----------------------------------------------------------------------
    // [8] View Reservation Status
    // -----------------------------------------------------------------------

    private void viewReservationStatus() {
        User user = authService.getCurrentUser();

        try {
            List<Reservation> reservations = reservationService.getActiveReservations(user.getId());

            ConsoleUI.printHeader("VIEW RESERVATION STATUS");
            System.out.println();
            System.out.printf("  Member: %s [%s]%n", user.getDisplayName(), user.getMemberId());
            System.out.printf("  Active Reservations: %d%n%n", reservations.size());

            if (reservations.isEmpty()) {
                System.out.println("  You have no active reservations.");
            } else {
                System.out.println("  " + ConsoleUI.DASH_62);
                System.out.printf("  %-4s | %-28s | %-10s | %-6s | %-20s%n",
                    "#", "Title", "Res. Date", "Queue", "Status");
                System.out.println("  " + ConsoleUI.DASH_62);
                for (int i = 0; i < reservations.size(); i++) {
                    Reservation r = reservations.get(i);
                    System.out.printf("  %-4d | %-28s | %-10s | %-6s | %-20s%n",
                        i + 1,
                        ConsoleUI.col(r.getBookTitle(), 28),
                        r.getResDate(),
                        r.getQueueDisplay(),
                        r.getStatus());
                }
                System.out.println("  " + ConsoleUI.DASH_62);
            }

            System.out.println();
            System.out.println("  ACTIONS:");
            System.out.println("    [1] Cancel a Reservation");
            System.out.println("    [2] Back to Dashboard");
            System.out.println();

            String choice = ConsoleUI.readLine("  Enter choice: ");
            if (choice.equals("1")) cancelReservation(reservations);

        } catch (SQLException e) {
            ConsoleUI.error("Database error: " + e.getMessage());
            ConsoleUI.pressEnter();
        }
    }

    private void cancelReservation(List<Reservation> reservations) {
        if (reservations.isEmpty()) {
            ConsoleUI.error("No active reservations to cancel.");
            ConsoleUI.pressEnter(); return;
        }
        System.out.println();
        String input = ConsoleUI.readLine("  Enter # of reservation to cancel: ");
        try {
            int idx = Integer.parseInt(input) - 1;
            if (idx < 0 || idx >= reservations.size()) {
                ConsoleUI.error("Invalid selection."); ConsoleUI.pressEnter(); return;
            }
            Reservation r = reservations.get(idx);
            String confirm = ConsoleUI.readConfirm(
                "  Cancel reservation for \"" + r.getBookTitle() + "\"? [Y/N]: ");
            if (confirm.equals("N")) { ConsoleUI.info("Cancellation aborted."); ConsoleUI.pressEnter(); return; }

            boolean ok = reservationService.cancelReservation(
                r.getId(), authService.getCurrentUser().getId());
            if (ok) ConsoleUI.success("Reservation cancelled.");
            else    ConsoleUI.error("Failed to cancel reservation.");

        } catch (NumberFormatException e) {
            ConsoleUI.error("Invalid input.");
        } catch (SQLException e) {
            ConsoleUI.error("Database error: " + e.getMessage());
        }
        ConsoleUI.pressEnter();
    }

    // -----------------------------------------------------------------------
    // [9] View Borrowing History
    // -----------------------------------------------------------------------

    private void viewBorrowingHistory() {
        User user = authService.getCurrentUser();

        try {
            List<Loan> history = loanService.getBorrowingHistory(user.getId());

            ConsoleUI.printHeader("VIEW BORROWING HISTORY");
            System.out.println();
            System.out.printf("  Member: %s [%s]%n", user.getDisplayName(), user.getMemberId());
            System.out.printf("  Total Borrows: %d%n%n", history.size());

            if (history.isEmpty()) {
                System.out.println("  No borrowing history yet.");
            } else {
                System.out.println("  " + ConsoleUI.DASH_62);
                System.out.printf("  %-4s | %-28s | %-10s | %-10s | %-8s%n",
                    "#", "Title", "Borrowed", "Returned", "Status");
                System.out.println("  " + ConsoleUI.DASH_62);
                for (int i = 0; i < history.size(); i++) {
                    Loan l = history.get(i);
                    String returned = l.getReturnedDate() != null ? l.getReturnedDate() : "-";
                    System.out.printf("  %-4d | %-28s | %-10s | %-10s | %-8s%n",
                        i + 1,
                        ConsoleUI.col(l.getBookTitle(), 28),
                        l.getBorrowedDate(),
                        returned,
                        l.getStatus());
                }
                System.out.println("  " + ConsoleUI.DASH_62);
            }

            ConsoleUI.pressEnter();

        } catch (SQLException e) {
            ConsoleUI.error("Database error: " + e.getMessage());
            ConsoleUI.pressEnter();
        }
    }

    // -----------------------------------------------------------------------
    // [10] View Returned Books
    // -----------------------------------------------------------------------

    private void viewReturnedBooks() {
        User user = authService.getCurrentUser();

        try {
            List<Loan> returned = loanService.getReturnedLoans(user.getId());

            ConsoleUI.printHeader("VIEW RETURNED BOOKS");
            System.out.println();
            System.out.printf("  Member: %s [%s]%n", user.getDisplayName(), user.getMemberId());
            System.out.printf("  Total Returns: %d%n%n", returned.size());

            if (returned.isEmpty()) {
                System.out.println("  You have no returned books yet.");
            } else {
                System.out.println("  " + ConsoleUI.DASH_62);
                System.out.printf("  %-4s | %-28s | %-10s | %-10s | %-10s%n",
                    "#", "Title", "Borrowed", "Returned", "Return ID");
                System.out.println("  " + ConsoleUI.DASH_62);
                for (int i = 0; i < returned.size(); i++) {
                    Loan l = returned.get(i);
                    String returnId = l.getReturnId() != null ? l.getReturnId() : "-";
                    System.out.printf("  %-4d | %-28s | %-10s | %-10s | %-10s%n",
                        i + 1,
                        ConsoleUI.col(l.getBookTitle(), 28),
                        l.getBorrowedDate(),
                        l.getReturnedDate(),
                        returnId);
                }
                System.out.println("  " + ConsoleUI.DASH_62);
            }

            ConsoleUI.pressEnter();

        } catch (SQLException e) {
            ConsoleUI.error("Database error: " + e.getMessage());
            ConsoleUI.pressEnter();
        }
    }
}
