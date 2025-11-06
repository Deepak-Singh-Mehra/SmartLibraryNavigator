// common
const API = 'http://localhost:8000';

// ---------------- Auth & Registration ----------------
function register(){
  const id = document.getElementById('rid').value.trim();
  const name = document.getElementById('rname').value.trim();
  const pass = document.getElementById('rpass').value.trim();
  if(!id || !name || !pass){ document.getElementById('reg-msg').innerText='All fields required'; return; }
  fetch(`${API}/student/register`, {
    method:'POST', headers:{'Content-Type':'application/json'},
    body: JSON.stringify({studentId:id,name:name,password:pass})
  }).then(r=>r.text()).then(msg=>{ document.getElementById('reg-msg').innerText = msg; if(msg.startsWith('Registration')) setTimeout(()=>location.href='login.html?mode=student',1000); })
}

function login(){
  const id = document.getElementById('userid').value.trim();
  const pass = document.getElementById('password').value.trim();
  fetch(`${API}/student/login`, {
    method:'POST', headers:{'Content-Type':'application/json'},
    body: JSON.stringify({studentId:id,password:pass})
  }).then(r=>r.text()).then(msg=>{
    const el = document.getElementById('login-msg');
    if(el) el.innerText = msg;
    if(msg === 'Login successful'){ localStorage.setItem('studentId', id); location.href='student.html'; }
  }).catch(()=>{ if(document.getElementById('login-msg')) document.getElementById('login-msg').innerText='Server unreachable'; });
}

function logout(){ localStorage.removeItem('studentId'); location.href='index.html'; }
function adminLogout(){ localStorage.removeItem('admin'); location.href='index.html'; }

// ---------------- Admin operations ----------------
function getBookForm(){
  return {
    id: document.getElementById('bookId').value.trim(),
    title: document.getElementById('title').value.trim(),
    author: document.getElementById('author').value.trim(),
    category: document.getElementById('category').value.trim(),
    shelf: document.getElementById('shelf').value.trim(),
    totalCopies: parseInt(document.getElementById('copies').value) || 1
  };
}

function addBook(){
  const book = getBookForm();
  fetch(`${API}/admin/add`, {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(book)})
    .then(r=>r.text()).then(m=>document.getElementById('admin-msg').innerText = m);
}

function updateBook(){
  const book = getBookForm();
  fetch(`${API}/admin/update`, {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(book)})
    .then(r=>r.text()).then(m=>document.getElementById('admin-msg').innerText = m);
}

function issueBook(){
  const bookId = document.getElementById('issueBookId').value.trim();
  const studentId = document.getElementById('studentId').value.trim();
  fetch(`${API}/admin/issue`, {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({bookId, studentId})})
    .then(r=>r.text()).then(m=>document.getElementById('admin-msg').innerText = m);
}

function returnBook(){
  const bookId = document.getElementById('issueBookId').value.trim();
  const studentId = document.getElementById('studentId').value.trim();
  fetch(`${API}/admin/return`, {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({bookId, studentId})})
    .then(r=>r.text()).then(m=>document.getElementById('admin-msg').innerText = m);
}

// ---------------- Student features ----------------
function autoSuggest(){
  const prefix = document.getElementById('searchTitle').value.trim();
  if(!prefix){ document.getElementById('suggestions').innerHTML=''; return; }
  fetch(`${API}/autosuggest?prefix=${encodeURIComponent(prefix)}`)
    .then(r=>r.json()).then(list=>{
      const ul = document.getElementById('suggestions'); ul.innerHTML='';
      list.forEach(t=>{
        const li = document.createElement('li'); li.innerText = t; li.onclick = ()=>{ document.getElementById('searchTitle').value = t; searchBook(); }; ul.appendChild(li);
      });
    });
}

function searchBook(){
  const title = document.getElementById('searchTitle').value.trim();
  if(!title) return;
  fetch(`${API}/search?title=${encodeURIComponent(title)}`)
    .then(r=>r.json()).then(book=>{
      if (book === "Book not found") { document.getElementById('book-details').innerText = book; return; }
      // book is an object with id etc.
      fetch(`${API}/student/bookdetails?bookId=${book.id}`).then(r=>r.json()).then(data=>{
        const b = data.book;
        const path = data.path || [];
        document.getElementById('book-details').innerHTML = `
          <p><strong>Title:</strong> ${b.title}</p>
          <p><strong>Author:</strong> ${b.author}</p>
          <p><strong>Category:</strong> ${b.category}</p>
          <p><strong>Shelf:</strong> ${b.shelf}</p>
          <p><strong>Available:</strong> ${b.totalCopies - b.issuedCount}</p>
          <p><strong>Path:</strong> ${path.join(' → ')}</p>
        `;
      });
    });
}

function getRecommendations(){
  fetch(`${API}/student/recommendations`).then(r=>r.json()).then(list=>{
    const ul = document.getElementById('recommendations'); if(!ul) return; ul.innerHTML='';
    list.forEach(b => { const li=document.createElement('li'); li.innerText = `${b.title} (issued:${b.issuedCount})`; ul.appendChild(li); });
  });
}

function loadUserIssued(id){
  // read students.json to show issued books of user
  fetch('data/students.json').then(r=>r.json()).then(arr=>{
    const user = arr.find(x=>x.studentId===id);
    const el = document.getElementById('user-info');
    if (!user) { el.innerHTML = `<p>User not found</p>`; return; }
    let html = `<p>Name: ${user.name}</p><p>ID: ${user.studentId}</p><h4>Issued Books</h4>`;
    html += '<ul>';
    (user.issuedBooks||[]).forEach(bid => html += `<li>${bid}</li>`);
    html += '</ul>';
    el.innerHTML = html;
  });
}

function loadMostIssued(){
  fetch(`${API}/student/recommendations`).then(r=>r.json()).then(list=>{
    const ul = document.getElementById('most-issued'); if(!ul) return; ul.innerHTML='';
    list.slice(0,5).forEach(b=>{ const li=document.createElement('li'); li.innerText = `${b.title} (${b.issuedCount})`; ul.appendChild(li); });
  });
}
