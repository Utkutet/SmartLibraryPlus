package app;

import dao.BookDao;
import dao.LoanDao;
import dao.StudentDao;
import entity.Book;
import entity.Loan;
import entity.Student;
import util.HibernateUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final BookDao bookDao = new BookDao();
    private static final StudentDao studentDao = new StudentDao();
    private static final LoanDao loanDao = new LoanDao();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Initialize Hibernate to ensure DB is ready
        HibernateUtil.getSessionFactory();

        boolean running = true;
        while (running) {
            System.out.println("\n=== SmartLibraryPlus Sistemi ===");
            System.out.println("1. Kitap Ekle");
            System.out.println("2. Kitapları Listele");
            System.out.println("3. Öğrenci Ekle");
            System.out.println("4. Öğrencileri Listele");
            System.out.println("5. Kitap Ödünç Ver");
            System.out.println("6. Ödünç Listesini Görüntüle");
            System.out.println("7. Kitap Geri Teslim Al");
            System.out.println("0. Çıkış");
            System.out.print("Seçiminiz: ");

            int choice = -1;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Geçersiz giriş. Lütfen bir sayı girin.");
                continue;
            }

            switch (choice) {
                case 1 -> addBook();
                case 2 -> listBooks();
                case 3 -> addStudent();
                case 4 -> listStudents();
                case 5 -> borrowBook();
                case 6 -> viewLoans();
                case 7 -> returnBook();
                case 0 -> {
                    running = false;
                    System.out.println("Çıkış yapılıyor...");
                }
                default -> System.out.println("Geçersiz seçim. Tekrar deneyin.");
            }
        }

        HibernateUtil.shutdown();
        scanner.close();
    }

    private static void addBook() {
        System.out.print("Kitap Başlığı (Title): ");
        String title = scanner.nextLine();
        System.out.print("Yazar (Author): ");
        String author = scanner.nextLine();
        System.out.print("Yıl (Year): ");
        int year;
        try {
            year = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Geçersiz yıl.");
            return;
        }

        Book book = new Book(title, author, year, Book.BookStatus.AVAILABLE);
        bookDao.save(book);
        System.out.println("Kitap başarıyla eklendi!");
    }

    private static void listBooks() {
        List<Book> books = bookDao.getAll();
        if (books.isEmpty()) {
            System.out.println("Kayıtlı kitap bulunamadı.");
        } else {
            System.out.printf("%-5s %-30s %-20s %-10s %-15s%n", "ID", "Başlık", "Yazar", "Yıl", "Durum");
            System.out.println("------------------------------------------------------------------------------------");
            for (Book b : books) {
                String statusStr = (b.getStatus() == Book.BookStatus.AVAILABLE) ? "MEVCUT" : "ÖDÜNÇ VERİLDİ";
                System.out.printf("%-5d %-30s %-20s %-10d %-15s%n",
                        b.getId(),
                        truncate(b.getTitle(), 30),
                        truncate(b.getAuthor(), 20),
                        b.getYear(),
                        statusStr);
            }
        }
    }

    private static void addStudent() {
        System.out.print("Öğrenci Adı: ");
        String name = scanner.nextLine();
        System.out.print("Bölüm (Department): ");
        String dept = scanner.nextLine();

        Student student = new Student(name, dept);
        studentDao.save(student);
        System.out.println("Öğrenci başarıyla eklendi!");
    }

    private static void listStudents() {
        List<Student> students = studentDao.getAll();
        if (students.isEmpty()) {
            System.out.println("Kayıtlı öğrenci bulunamadı.");
        } else {
            System.out.printf("%-5s %-20s %-20s%n", "ID", "Ad", "Bölüm");
            System.out.println("--------------------------------------------------");
            for (Student s : students) {
                System.out.printf("%-5d %-20s %-20s%n", s.getId(), truncate(s.getName(), 20),
                        truncate(s.getDepartment(), 20));
            }
        }
    }

    private static void borrowBook() {
        System.out.print("Öğrenci ID: ");
        Long studentId = parseLongInput();
        if (studentId == null)
            return;

        System.out.print("Kitap ID: ");
        Long bookId = parseLongInput();
        if (bookId == null)
            return;

        Student student = studentDao.getById(studentId);
        if (student == null) {
            System.out.println("Öğrenci bulunamadı.");
            return;
        }

        Book book = bookDao.getById(bookId);
        if (book == null) {
            System.out.println("Kitap bulunamadı.");
            return;
        }

        if (book.getStatus() == Book.BookStatus.BORROWED) {
            System.out.println("Hata: Bu kitap zaten ödünç verilmiş!");
            return;
        }

        // Proceed to borrow
        Loan loan = new Loan(student, book, LocalDate.now());
        loanDao.save(loan);

        book.setStatus(Book.BookStatus.BORROWED);
        bookDao.update(book);

        System.out.println("Kitap başarıyla ödünç verildi!");
    }

    private static void viewLoans() {
        List<Loan> loans = loanDao.getAll();
        if (loans.isEmpty()) {
            System.out.println("Ödünç kaydı bulunamadı.");
        } else {
            System.out.printf("%-5s %-20s %-30s %-15s %-15s%n", "ID", "Öğrenci", "Kitap", "Alış Tarihi", "İade Tarihi");
            System.out.println(
                    "-----------------------------------------------------------------------------------------");
            for (Loan l : loans) {
                String returnDateStr = (l.getReturnDate() == null) ? "Teslim Edilmedi" : l.getReturnDate().toString();
                System.out.printf("%-5d %-20s %-30s %-15s %-15s%n",
                        l.getId(),
                        truncate(l.getStudent().getName(), 20),
                        truncate(l.getBook().getTitle(), 30),
                        l.getBorrowDate(),
                        returnDateStr);
            }
        }
    }

    private static void returnBook() {
        System.out.print("İade edilecek Loan (Ödünç) ID: ");
        Long loanId = parseLongInput();
        if (loanId == null)
            return;

        Loan loan = loanDao.getById(loanId);
        if (loan == null) {
            System.out.println("Ödünç kaydı bulunamadı.");
            return;
        }

        if (loan.getReturnDate() != null) {
            System.out.println("Bu kitap zaten iade edilmiş.");
            return;
        }

        // Update Loan
        loan.setReturnDate(LocalDate.now());
        loanDao.update(loan);

        // Update Book
        Book book = loan.getBook();
        if (book != null) {
            book.setStatus(Book.BookStatus.AVAILABLE);
            bookDao.update(book);
        }

        System.out.println("Kitap başarıyla iade alındı!");
    }

    private static Long parseLongInput() {
        try {
            return Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Geçersiz ID formatı.");
            return null;
        }
    }

    private static String truncate(String str, int width) {
        if (str == null)
            return "";
        if (str.length() > width) {
            return str.substring(0, width - 3) + "...";
        }
        return str;
    }
}
