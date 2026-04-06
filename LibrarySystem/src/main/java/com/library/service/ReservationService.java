package com.library.service;

import com.library.model.Book;
import com.library.model.Reservation;
import com.library.repository.BookRepository;
import com.library.repository.ReservationRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReservationService {

    private final ReservationRepository resRepo  = new ReservationRepository();
    private final BookRepository        bookRepo = new BookRepository();

    public enum ReserveResult {
        SUCCESS, BOOK_NOT_FOUND, ALREADY_RESERVED, ERROR
    }

    // -----------------------------------------------------------------------

    public ReserveResult reserveBook(int userId, int bookId) throws SQLException {
        Book book = bookRepo.findById(bookId);
        if (book == null) return ReserveResult.BOOK_NOT_FOUND;
        if (resRepo.hasActiveReservation(userId, bookId)) return ReserveResult.ALREADY_RESERVED;

        String today = LocalDate.now().toString();
        resRepo.createReservation(userId, bookId, today);
        return ReserveResult.SUCCESS;
    }

    public List<Reservation> getActiveReservations(int userId) throws SQLException {
        return resRepo.findActiveByUserId(userId);
    }

    public boolean cancelReservation(int resId, int userId) throws SQLException {
        return resRepo.cancelReservation(resId, userId);
    }

    public boolean hasAvailableCopies(int bookId) throws SQLException {
        Book book = bookRepo.findById(bookId);
        return book != null && book.getAvailableCopies() > 0;
    }
}
