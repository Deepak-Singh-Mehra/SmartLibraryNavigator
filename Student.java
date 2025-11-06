package backend;

import java.util.*;

public class Student {
    public String studentId;
    public String name;
    public String password;
    public List<String> issuedBooks = new ArrayList<>();

    public Student() {}

    public Student(String studentId, String name, String password) {
        this.studentId = studentId; this.name = name; this.password = password;
        this.issuedBooks = new ArrayList<>();
    }

    public boolean login(String inputPassword) {
        return this.password != null && this.password.equals(inputPassword);
    }
}
