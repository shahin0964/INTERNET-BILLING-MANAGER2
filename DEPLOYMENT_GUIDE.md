# Internet Billing Manager 2 - Web & Cloud Deployment Guide

এই গাইডে **Internet Billing Manager 2**-এর সেন্ট্রাল ব্যাকএন্ড (`server/`), ওয়েব পোর্টাল (`web/`) এবং অ্যান্ড্রয়েড অ্যাপের ক্লাউড সিঙ্ক্রোনাইজেশন সেটআপ করার সম্পূর্ণ নির্দেশনা দেওয়া হলো।

---

## ১. আর্কিটেকচার ওভারভিউ (Architecture Overview)

```
       +---------------------------------------------+
       |             Cloud VPS / Local Server        |
       |                                             |
       |  +---------------------------------------+  |
       |  |  Node.js (TypeScript) Express Server  |  |
       |  |  Port: 5000                           |  |
       |  |  - REST API (/api/*)                  |  |
       |  |  - Web Portal Static Host (/*)        |  |
       |  +-------------------+-------------------+  |
       |                      |                      |
       |  +-------------------+-------------------+  |
       |  |  PostgreSQL 16 Database               |  |
       |  |  Port: 5432                           |  |
       |  +---------------------------------------+  |
       +----------------------+----------------------+
                              ^
            REST API (HTTPS)  |  Sync API (/api/sync)
                              v
        +---------------------+---------------------+
        |                                           |
+-------+---------------+                   +-------+---------------+
|   Web Browser / PC    |                   |   Android Mobile App  |
|   (Admin / Manager)   |                   |   (Billing Collector) |
+-----------------------+                   +-----------------------+
```

1. **Web Software (`web/`)**: ড্যাশবোর্ড, গ্রাহক পরিচালনা, বিল তৈরি, পেমেন্ট আদায় ও ল্যান্ডস্কেপ মানি রিসিট প্রিন্টিং সুবিধা।
2. **REST API Server (`server/`)**: PostgreSQL এর সাথে যুক্ত নিরাপদ TypeScript API ইঞ্জিন।
3. **Android App (`app/`)**: মাঠপর্যায়ে ইন্টারনেট কালেকশন করার অফলাইন-ফার্স্ট মোবাইল অ্যাপ। এটি `Settings -> ক্লাউড সার্ভার সিঙ্ক`-এর মাধ্যমে সেন্ট্রাল সার্ভারের সাথে স্বয়ংক্রিয়ভাবে সিঙ্ক হয়।

---

## ২. দ্রুত শুরু (Quick Start - Local / Termux Development)

### ২.১ প্রয়োজনীয় ডিপেন্ডেন্সি
- Node.js (v18+)
- pnpm (`pnpm -v`)
- PostgreSQL (অথবা Docker)

### ২.২ ব্যাকএন্ড সার্ভার চালু করা:
```bash
cd server
pnpm install
pnpm run build
pnpm start
```
ডিফল্টভাবে সার্ভার `http://localhost:5000` পোর্টে রান হবে। 
ব্রাউজারে `http://localhost:5000` ওপেন করলেই ওয়েব পোর্টালটি দেখতে পাবেন।

### ডিফল্ট লগইন ক্রেডেনশিয়াল:
- **Username / Mobile**: `admin`
- **Password**: `admin123`

*(প্রথমবার লগইন করার পর সেটিংস পেজ থেকে পাসওয়ার্ড পরিবর্তন করে নিন)*

---

## ৩. ক্লাউড ভিপিএস-এ ১-ক্লিক ডেপ্লয়মেন্ট (Docker Compose)

যেকোনো ক্লাউড সার্ভারে (DigitalOcean, Linode, AWS, Hetzner, VPS) সহজেই এটি ডেপ্লয় করতে পারবেন:

### ধাপ ১: রিপোজিটরি ক্লোন করুন
```bash
git clone https://github.com/your-username/INTERNET-BILLING-MANAGER2.git
cd INTERNET-BILLING-MANAGER2
```

### ধাপ ২: এনভায়রনমেন্ট ফাইল সেট করুন
`server/.env` ফাইলে আপনার ডাটাবেস পাসওয়ার্ড ও JWT সিক্রেট পরিবর্তন করুন:
```env
PORT=5000
DATABASE_URL=postgres://postgres:secure_db_password_2026@postgres:5432/internet_billing_db
JWT_SECRET=generate_a_super_secret_key_here
NODE_ENV=production
```

### ধাপ ৩: ডকার দিয়ে চালু করুন
```bash
docker compose up -d --build
```
এটি স্বয়ংক্রিয়ভাবে:
1. PostgreSQL 16 ডাটাবেস কনটেইনার চালু করবে।
2. টেবিল স্কিমা ও ডিফল্ট এডমিন ইউজার তৈরি করবে।
3. Node.js API ও Web Portal চালু করে পোর্ট `5000` এ হোস্ট করবে।

স্ট্যাটাস দেখতে:
```bash
docker compose ps
docker compose logs -f app
```

---

## ৪. অ্যান্ড্রয়েড অ্যাপ থেকে ক্লাউড সার্ভার সংযোগ (Mobile App Sync)

1. মোবাইলে **Internet Billing Manager 2** অ্যাপটি ওপেন করুন।
2. **Settings (সেটিংস)** ট্যাবে যান।
3. **ক্লাউড সার্ভার সিঙ্ক (Web & API)** অপশনে ট্যাপ করুন।
4. আপনার সার্ভারের URL দিন (যেমন: `http://192.168.1.100:5000` অথবা আপনার ডোমেন `https://billing.yourisp.com`)।
5. আপনার এডমিন মোবাইল নম্বর এবং পাসওয়ার্ড দিন।
6. **"টেস্ট সংযোগ"** বাটনে চাপ দিয়ে সংযোগ নিশ্চিত করুন।
7. **"এখনই সিঙ্ক করুন"** চাপলে ফোনের সব কাস্টমার, প্যাকেজ ও পেমেন্ট সার্ভারে আপলোড হবে এবং সার্ভারের নতুন ডাটা ফোনে চলে আসবে!

---

## ৫. ডিরেক্টরি পরিচিতি (Project Structure)

```
INTERNET-BILLING-MANAGER2/
├── app/                  # Android Kotlin Jetpack Compose Mobile App
│   ├── src/main/java/.../util/ServerSyncManager.kt   # Mobile Sync Engine
│   └── src/main/java/.../ui/components/ServerSyncDialog.kt # Mobile Sync UI
├── server/               # Node.js + TypeScript REST API
│   ├── sql/schema.sql    # PostgreSQL Schema
│   ├── src/routes/       # API Endpoints (Auth, Customers, Bills, Payments, Sync)
│   ├── src/services/     # FIFO Payment, MikroTik RouterOS Manager
│   └── Dockerfile        # Production Docker Image
├── web/                  # Web Software SPA
│   ├── index.html        # Glassmorphic UI Dashboard & Billing POS
│   ├── css/style.css     # Obsidian Theme & Print Receipt Stylesheet
│   └── js/app.js         # Frontend Controller & Print Engine
└── docker-compose.yml    # 1-Click Production Orchestration
```
