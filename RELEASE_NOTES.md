# 📱 INTERNET BILLING MANAGER (IBM) - v1.0.50

🎨 কাস্টমার অ্যাড ও সকল ডায়ালগের ডার্ক থিম টেক্সট কন্ট্রাস্ট ফিক্স (Customer Add Dialog & All Modal Dialogs Theme Contrast Fix):
• **ডার্ক থিমে স্পষ্ট ও নিখুঁত ইনপুট ফিল্ড (High-Contrast Input Fields in Dark Themes):**
  - Liquid Glass (লাইট থিম) ব্যতীত বাকি সকল ডার্ক থিমে (`Midnight Cyber`, `AMOLED Pitch`, `Emerald Cyber`, `Royal Nebula`, `Sunset Amber`) কাস্টমার অ্যাড/এডিট ডায়ালগ এবং অন্যান্য ডায়ালগের ফিল্ডগুলোতে টেক্সট কালো হওয়ার সমস্যা স্থায়ীভাবে সমাধান করা হয়েছে।
  - প্রতিটি ডায়ালগকে সরাসরি `InternetBillingTheme` ও `Surface(..., contentColor = TextPrimary)` দিয়ে র‍্যাপ করা হয়েছে যাতে কোনো ডায়ালগই অ্যান্ড্রয়েডের ডিফল্ট লাইট-মোড ব্যাকগ্রাউন্ড বা কালার ব্যবহার না করে।
  - `CustomerDialogs.kt`-এর সকল ইনপুট ফিল্ড (Customer Name, Phone Number, Customer Code, PPPoE Username, Static IP, Address, Notes, Monthly Fee, Bill Due Day, Previous Due Input) এ ডায়নামিক `dialogTextFieldColors` এবং `dialogTextStyle` প্রয়োগ করা হয়েছে যাতে টাইপ করা টেক্সট উজ্জ্বল সাদা, লেবেল স্পষ্ট সায়ান/সেকেন্ডারি এবং প্লেসহোল্ডার পরিচ্ছন্ন ধূসর রঙে দেখা যায়।

• **সকল অ্যাকশন ও ফিচার ডায়ালগ আপডেট (All Action & Feature Dialogs Updated):**
  - **Manual Add Bill Dialog**: কাস্টমার নির্বাচন ড্রপডাউন, বিলিং মাস, বিল অ্যামাউন্ট এবং নোটস ফিল্ডের হাই-কন্ট্রাস্ট থিমিং সম্পূর্ণ।
  - **Payment Collection Dialog**: পেমেন্ট অ্যামাউন্ট ও নোটস ইনপুট ফিল্ড থিম কালার ফিক্স।
  - **Add Expense Dialog**: খরচের নাম, ক্যাটাগরি, অ্যামাউন্ট ও নোট ফিল্ডে উজ্জ্বল বেগুনি ও সাদা কালার স্কিম যুক্ত।
  - **Edit Business Info Dialog**: প্রতিষ্ঠানের নাম, ফোন, কারেন্সি, ইমেইল ও ঠিকানা ফিল্ডের নিখুঁত কন্ট্রাস্ট ও ফিল্ড ব্যাকগ্রাউন্ড।
  - **Packages Manager & Add Package Dialog**: ইন্টারনেট প্যাকেজের নাম, স্পিড ও মাসিক মূল্যের ইনপুট ফিল্ড এবং ডায়ালগ বর্ডার থিম ফিক্স।
  - **Theme Selection & Language Selection Dialogs**: থিম এবং ভাষা পরিবর্তনের পপআপ উইন্ডোতেও পুরো থিম সিস্টেম সামঞ্জস্যপূর্ণ করা হয়েছে।
  - **App Update & About Dialog**: অ্যাপ আপডেট ও সম্পর্কিত তথ্যের উইন্ডো ডার্ক থিমে ক্রিস্টাল ক্লিয়ার টেক্সট প্রদর্শন নিশ্চিত করা হয়েছে।
