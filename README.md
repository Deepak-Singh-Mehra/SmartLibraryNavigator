# SmartLibraryNavigator
:: compile
javac -cp ".;backend\lib\gson-2.13.1.jar" backend\*.java

:: run
java -cp ".;backend\lib\gson-2.13.1.jar;backend" backend.Main

structure:
SmartLibraryNavigator/
├─ backend/
│  ├─ lib/
│  │  └─ gson-2.13.1.jar
│  ├─ Admin.java
│  ├─ AVLTree.java
│  ├─ Book.java
│  ├─ Graph.java
│  ├─ Main.java
│  ├─ Student.java
│  └─ Trie.java
├─ frontend/
│  ├─ index.html
│  ├─ register.html
│  ├─ login.html
│  ├─ student.html
│  ├─ admin.html
│  ├─ style.css
│  └─ script.js
└─ data/
   ├─ books.json
   └─ students.json

