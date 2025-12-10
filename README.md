# KRS-Mama Palm Oil Transaction System (Android Application)

KRS-Mama is a mobile application designed to streamline palm oil trading operations, specifically for small to medium-scale collection centers (RAM/Sentra Sawit). The app digitizes manual weight-based transactions, automates calculation of deductions, manages customer debts, and generates printable digital invoices (PDF).  
This system helps reduce human error, improve operational efficiency, and ensure accurate financial records for daily field operations.

---

## 📌 Key Features

### 🔹 **1. Create & Manage Purchase Notes (Nota Sawit)**
- Input masuk (gross weight) and keluar (tare weight)
- Automatic calculation for netto, percentage cuts, subtotal, and total received
- Optional cost deductions: Muat/Supir, Ampang-Ampang, Potong Utang
- Real-time total calculation

### 🔹 **2. PDF Invoice & Print-Ready Preview**
- Instant PDF generation from nota
- Clean preview screen before printing
- PDF automatically saved to device storage
- Optional direct print (if printer available)

### 🔹 **3. Customer Management System**
- Add, edit, and view customer data
- Stores customer details, address, land info, and current debt balance
- One-tap access to customer transaction history

### 🔹 **4. Debt Tracking & Payment History**
- Automatic deduction when “Potong Utang” is applied in nota
- Manual entry for customer payments (setoran)
- Full history log showing all debt-related transactions

### 🔹 **5. Dashboard Overview**
Provides real-time financial summary:
- Total Pendapatan (Revenue)
- Total Pengeluaran (Expenses)
- Keuntungan Bersih (Profit)
- Total Utang Pelanggan (Outstanding Debts)
- Recent transaction list with CRUD controls

---

## 🛠 Tools & Technologies Used

### **Development Environment**
- Android Studio (latest version)
- Java (Android development)
- Minimum SDK: 21 (Android 5.0)

### **Backend & Database**
- Firebase Authentication (login/registration)
- Firebase Firestore (transactions, customers, debt logs)
- Firebase Storage (optional PDF storage)

### **UI/UX**
- XML Layouts (Material Design)
- Custom card views and components
- Light green theme optimized for readability outdoors

### **Additional Libraries**
- RecyclerView (listing data)
- DatePicker & TimePicker dialogs
- PDF generation utilities
- SharedPreferences (local caching)

---

## Developed by Anjelin

