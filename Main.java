package backend;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import com.google.gson.*;

public class Main {

    public static AVLTree bookTree = new AVLTree();
    public static Trie bookTrie = new Trie();
    public static Graph shelfGraph = new Graph();
    public static Map<String, Student> students = new HashMap<>();
    public static Map<String, Book> books = new HashMap<>();

    public static void main(String[] args) throws Exception {
        // Ensure data folder exists
        new File("data").mkdirs();

        loadBooks();
        loadStudents();
        setupShelfGraph(); // initializes adjacency

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Serve frontend from root
        server.createContext("/", exchange -> {
            handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.equals("")) path = "/index.html";
            File file = new File("./frontend" + path);
            if (!file.exists() || file.isDirectory()) {
                String nf = "404 Not Found";
                exchange.getResponseHeaders().add("Content-Type", "text/plain");
                exchange.sendResponseHeaders(404, nf.getBytes().length);
                OutputStream os = exchange.getResponseBody(); os.write(nf.getBytes()); os.close(); return;
            }
            String mime = "text/html";
            if (path.endsWith(".css")) mime = "text/css";
            else if (path.endsWith(".js")) mime = "application/javascript";
            else if (path.endsWith(".json")) mime = "application/json";
            exchange.getResponseHeaders().add("Content-Type", mime);
            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody(); os.write(bytes); os.close();
        });

        // API endpoints
        server.createContext("/student/register", new StudentRegisterHandler());
        server.createContext("/student/login", new StudentLoginHandler());
        server.createContext("/search", new SearchHandler());
        server.createContext("/autosuggest", new AutoSuggestHandler());
        server.createContext("/student/bookdetails", new BookDetailsHandler());
        server.createContext("/student/recommendations", new RecommendationsHandler());

        server.createContext("/admin/add", new AddBookHandler());
        server.createContext("/admin/update", new UpdateBookHandler());
        server.createContext("/admin/issue", new IssueBookHandler());
        server.createContext("/admin/return", new ReturnBookHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Backend server running at http://localhost:8000");
    }

    public static void loadBooks() {
        try {
            File f = new File("data/books.json");
            if (!f.exists()) { Files.write(Paths.get("data/books.json"), "[]".getBytes()); }
            Gson gson = new Gson();
            Reader br = new FileReader("data/books.json");
            Book[] bookList = gson.fromJson(br, Book[].class);
            br.close();
            books.clear();
            if (bookList != null) for (Book b : bookList) {
                books.put(b.id, b);
                bookTree.insert(b);
                bookTrie.insert(b.title);
                shelfGraph.addBookShelf(b.shelf, b.id);
            }
            System.out.println("Books loaded: " + books.size());
        } catch (Exception e) { System.err.println("loadBooks error: " + e.getMessage()); }
    }

    public static void loadStudents() {
        try {
            File f = new File("data/students.json");
            if (!f.exists()) { Files.write(Paths.get("data/students.json"), "[]".getBytes()); }
            Gson gson = new Gson();
            Reader br = new FileReader("data/students.json");
            Student[] studentList = gson.fromJson(br, Student[].class);
            br.close();
            students.clear();
            if (studentList != null) for (Student s : studentList) students.put(s.studentId, s);
            // load issued_books if present
            File ib = new File("data/issued_books.json");
            if (ib.exists()) {
                Reader r2 = new FileReader(ib);
                Map<String, List<String>> issued = new Gson().fromJson(r2, new com.google.gson.reflect.TypeToken<Map<String, List<String>>>(){}.getType());
                r2.close();
                if (issued != null) for (Map.Entry<String, List<String>> e: issued.entrySet()) {
                    Student s = students.get(e.getKey()); if (s != null) s.issuedBooks = new ArrayList<>(e.getValue());
                }
            }
            System.out.println("Students loaded: " + students.size());
        } catch (Exception e) { System.err.println("loadStudents error: " + e.getMessage()); }
    }

    public static void setupShelfGraph() {
        // Example layout — you can expand
        shelfGraph.addEdge("Entrance","A1");
        shelfGraph.addEdge("A1","A2");
        shelfGraph.addEdge("A2","A3");
        shelfGraph.addEdge("A1","B1");
        shelfGraph.addEdge("B1","B2");
        shelfGraph.addEdge("B2","B3");
        System.out.println("Shelf graph initialized");
    }

    // CORS util
    private static void handleCORS(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin","*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods","GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers","Content-Type");
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204,-1);
        }
    }

    // ---------------- Handlers ----------------

    static class StudentRegisterHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) { exchange.sendResponseHeaders(405,-1); return; }
            try {
                String body = new String(exchange.getRequestBody().readAllBytes());
                Gson gson = new Gson();
                Student s = gson.fromJson(body, Student.class);
                String res = Admin.registerStudent(s);
                exchange.getResponseHeaders().add("Content-Type","text/plain");
                exchange.sendResponseHeaders(200, res.getBytes().length);
                OutputStream os = exchange.getResponseBody(); os.write(res.getBytes()); os.close();
            } catch (Exception e) {
                String msg = "Error: "+e.getMessage();
                exchange.sendResponseHeaders(500, msg.getBytes().length);
                OutputStream os = exchange.getResponseBody(); os.write(msg.getBytes()); os.close();
            }
        }
    }

    static class StudentLoginHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) { exchange.sendResponseHeaders(405,-1); return; }
            try {
                String body = new String(exchange.getRequestBody().readAllBytes());
                Gson gson = new Gson();
                Map<String,String> map = gson.fromJson(body, new com.google.gson.reflect.TypeToken<Map<String,String>>(){}.getType());
                String studentId = map.get("studentId");
                String password = map.get("password");
                String response = "Invalid credentials";
                Student s = students.get(studentId);
                if (s != null && s.login(password)) response = "Login successful";
                exchange.getResponseHeaders().add("Content-Type","text/plain");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody(); os.write(response.getBytes()); os.close();
            } catch (Exception e) {
                exchange.sendResponseHeaders(500, -1);
            }
        }
    }

    static class SearchHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            String q = exchange.getRequestURI().getQuery();
            if (q == null) { exchange.sendResponseHeaders(400,-1); return; }
            String title = q.split("=")[1];
            title = java.net.URLDecoder.decode(title, "UTF-8");
            Book found = bookTree.search(title);
            String response = new Gson().toJson(found != null ? found : "Book not found");
            exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody(); os.write(response.getBytes()); os.close();
        }
    }

    static class AutoSuggestHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            String q = exchange.getRequestURI().getQuery();
            if (q == null) { exchange.sendResponseHeaders(400,-1); return; }
            String prefix = q.split("=")[1];
            prefix = java.net.URLDecoder.decode(prefix,"UTF-8");
            List<String> suggestions = bookTrie.autoSuggest(prefix);
            String response = new Gson().toJson(suggestions);
            exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody(); os.write(response.getBytes()); os.close();
        }
    }

    static class BookDetailsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            String q = exchange.getRequestURI().getQuery();
            if (q == null) { exchange.sendResponseHeaders(400,-1); return; }
            String bookId = q.split("=")[1];
            bookId = java.net.URLDecoder.decode(bookId,"UTF-8");
            Book book = books.get(bookId);
            Map<String,Object> resp = new HashMap<>();
            if (book != null) {
                resp.put("book", book);
                resp.put("path", shelfGraph.shortestPath("Entrance", book.shelf));
            } else resp.put("message","Book not found");
            String response = new Gson().toJson(resp);
            exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody(); os.write(response.getBytes()); os.close();
        }
    }

    static class RecommendationsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            List<Book> top = new ArrayList<>(books.values());
            top.sort((a,b) -> b.issuedCount - a.issuedCount);
            List<Book> rec = top.size() > 5 ? top.subList(0,5) : top;
            String response = new Gson().toJson(rec);
            exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody(); os.write(response.getBytes()); os.close();
        }
    }

    // Admin endpoints utilize Admin class methods
    static class AddBookHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) { exchange.sendResponseHeaders(405,-1); return; }
            try {
                String body = new String(exchange.getRequestBody().readAllBytes());
                Book book = new Gson().fromJson(body, Book.class);
                String res = Admin.addBook(book);
                exchange.getResponseHeaders().add("Content-Type","text/plain");
                exchange.sendResponseHeaders(200, res.getBytes().length);
                OutputStream os = exchange.getResponseBody(); os.write(res.getBytes()); os.close();
            } catch (Exception e) {
                String msg = "Error: "+e.getMessage();
                exchange.sendResponseHeaders(500, msg.getBytes().length);
                OutputStream os = exchange.getResponseBody(); os.write(msg.getBytes()); os.close();
            }
        }
    }

    static class UpdateBookHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            try {
                String body = new String(exchange.getRequestBody().readAllBytes());
                Book book = new Gson().fromJson(body, Book.class);
                String res = Admin.updateBook(book);
                exchange.getResponseHeaders().add("Content-Type","text/plain");
                exchange.sendResponseHeaders(200, res.getBytes().length);
                OutputStream os = exchange.getResponseBody(); os.write(res.getBytes()); os.close();
            } catch (Exception e) {
                String msg = "Error: "+e.getMessage();
                exchange.sendResponseHeaders(500, msg.getBytes().length);
                OutputStream os = exchange.getResponseBody(); os.write(msg.getBytes()); os.close();
            }
        }
    }

    static class IssueBookHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            try {
                String body = new String(exchange.getRequestBody().readAllBytes());
                Map<String,String> map = new Gson().fromJson(body, new com.google.gson.reflect.TypeToken<Map<String,String>>(){}.getType());
                String res = Admin.issueBook(map.get("bookId"), map.get("studentId"));
                exchange.getResponseHeaders().add("Content-Type","text/plain");
                exchange.sendResponseHeaders(200, res.getBytes().length);
                OutputStream os = exchange.getResponseBody(); os.write(res.getBytes()); os.close();
            } catch (Exception e) {
                String msg = "Error: "+e.getMessage();
                exchange.sendResponseHeaders(500, msg.getBytes().length);
                OutputStream os = exchange.getResponseBody(); os.write(msg.getBytes()); os.close();
            }
        }
    }

    static class ReturnBookHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            handleCORS(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) return;
            try {
                String body = new String(exchange.getRequestBody().readAllBytes());
                Map<String,String> map = new Gson().fromJson(body, new com.google.gson.reflect.TypeToken<Map<String,String>>(){}.getType());
                String res = Admin.returnBook(map.get("bookId"), map.get("studentId"));
                exchange.getResponseHeaders().add("Content-Type","text/plain");
                exchange.sendResponseHeaders(200, res.getBytes().length);
                OutputStream os = exchange.getResponseBody(); os.write(res.getBytes()); os.close();
            } catch (Exception e) {
                String msg = "Error: "+e.getMessage();
                exchange.sendResponseHeaders(500, msg.getBytes().length);
                OutputStream os = exchange.getResponseBody(); os.write(msg.getBytes()); os.close();
            }
        }
    }

}
