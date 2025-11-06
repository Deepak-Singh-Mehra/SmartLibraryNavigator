package backend;

import java.io.*;
import java.util.*;
import com.google.gson.*;

public class Admin {

    // Add a new book
    public static synchronized String addBook(Book book) throws Exception {
        if (Main.books.containsKey(book.id)) {
            return "Book with ID already exists!";
        }
        Main.books.put(book.id, book);
        rebuildAVLAndTrie();
        Main.shelfGraph.addBookShelf(book.shelf, book.id);
        saveBooks();
        return "Book added: " + book.title;
    }

    // Update book information
    public static synchronized String updateBook(Book book) throws Exception {
        Book b = Main.books.get(book.id);
        if (b == null) {
            return "Book not found!";
        }
        b.title = book.title;
        b.author = book.author;
        b.category = book.category;
        b.shelf = book.shelf;
        b.totalCopies = book.totalCopies;
        rebuildAVLAndTrie();
        saveBooks();
        return "Book updated: " + b.title;
    }

    // Issue a book to a student (admin approves)
    public static synchronized String issueBook(String bookId, String studentId) throws Exception {
        Book b = Main.books.get(bookId);
        Student s = Main.students.get(studentId);
        if (b == null) return "Book not found!";
        if (s == null) return "Student not found!";
        if (b.issuedCount >= b.totalCopies) return "No copies available to issue!";
        if (s.issuedBooks.contains(bookId)) return "Student already has this book issued!";
        b.issuedCount++;
        s.issuedBooks.add(bookId);
        saveBooks();
        saveIssuedBooks();
        return "Book issued: " + b.title + " to " + s.studentId;
    }

    // Return a book from a student
    public static synchronized String returnBook(String bookId, String studentId) throws Exception {
        Book b = Main.books.get(bookId);
        Student s = Main.students.get(studentId);
        if (b == null || s == null || !s.issuedBooks.contains(bookId)) {
            return "Invalid return operation!";
        }
        b.issuedCount--;
        s.issuedBooks.remove(bookId);
        saveBooks();
        saveIssuedBooks();
        return "Book returned: " + b.title + " from " + s.studentId;
    }

    // Create new student account
    public static synchronized String registerStudent(Student student) throws Exception {
        if (Main.students.containsKey(student.studentId)) {
            return "Student ID already exists!";
        }
        Main.students.put(student.studentId, student);
        saveStudents();
        return "Registration successful for " + student.studentId;
    }

    private static void rebuildAVLAndTrie() {
        Main.bookTree = new AVLTree();
        Main.bookTrie = new Trie();
        for (Book b : Main.books.values()) {
            Main.bookTree.insert(b);
            Main.bookTrie.insert(b.title);
        }
    }

    private static void saveBooks() throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter fw = new FileWriter("data/books.json");
        gson.toJson(new ArrayList<>(Main.books.values()), fw);
        fw.close();
    }

    private static void saveIssuedBooks() throws Exception {
        Map<String, List<String>> issuedMap = new HashMap<>();
        for (Student s : Main.students.values()) issuedMap.put(s.studentId, s.issuedBooks);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter fw = new FileWriter("data/issued_books.json");
        gson.toJson(issuedMap, fw);
        fw.close();
    }

    private static void saveStudents() throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter fw = new FileWriter("data/students.json");
        gson.toJson(new ArrayList<>(Main.students.values()), fw);
        fw.close();
    }
}
