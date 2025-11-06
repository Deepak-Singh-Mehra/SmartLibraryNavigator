package backend;

public class Book {
    public String id;
    public String title;
    public String author;
    public String category;
    public String shelf; // e.g., "A1"
    public int totalCopies;
    public int issuedCount;

    // Default constructor needed for Gson
    public Book() {}

    public Book(String id, String title, String author, String category, String shelf, int totalCopies) {
        this.id = id; this.title = title; this.author = author; this.category = category;
        this.shelf = shelf; this.totalCopies = totalCopies; this.issuedCount = 0;
    }
}
